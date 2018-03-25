
local sub = string.sub
local escape_uri = ngx.escape_uri
local unescape_uri = ngx.unescape_uri
local match = string.match
local tcp = ngx.socket.tcp
local strlen = string.len
local concat = table.concat
local setmetatable = setmetatable
local type = type
local error = error

local _M = {
    _VERSION = '0.99'
}

local mt = { __index = _M }

function _M.new(self, opts)
    local sock, err = tcp()
    if not sock then
        return nil, err
    end

    local escape_key = escape_uri
    local unescape_key = unescape_uri

    if opts then
        local key_transform = opts.key_transform

        if key_transform then
            escape_key = key_transform[1]
            unescape_key = key_transform[2]
            if not escape_key or not unescape_key then
                return nil, "expecting key_transform = { escape, unescape } table"
            end
        end
    end

    return setmetatable({
        sock = sock,
        escape_key = escape_key,
        unescape_key = unescape_key,
    }, mt)
end


function _M.set_timeout(self, timeout)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    sock:settimeout(timeout)
    return 1
end


function _M.connect(self, ...)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    return sock:connect(...)
end



function _M.get(self, key)

    local sock = self.sock
    if not sock then
        return nil, nil, "not initialized"
    end

    ngx.log(ngx.DEBUG, "Request : ", "get " .. self.escape_key(key) .. "\r\n")

    local bytes, err = sock:send("get " .. self.escape_key(key) .. "\r\n" )
    if not bytes then
        return nil, nil, err
    end

    -- discard \r\n
    sock:receive()

    local line, err = sock:receive()
    if not line then
        if err == "timeout" then
            sock:close()
        end
        return nil, nil, err
    end

    ngx.log(ngx.DEBUG, "Line ", line)

    local len = match(line, '^LEN (%d+)$')
    if not len then
        return nil, nil, nil
    end

    ngx.log(ngx.DEBUG, "LEN ", len)

    local data, err = sock:receive(len)
    if not data then
        if err == "timeout" then
            sock:close()
        end
        return nil, nil, err
    end

    ngx.log(ngx.DEBUG, "Data : ", data)

    line, err = sock:receive(8) -- discard the trailing "\r\nDONE\r\n"
    if not line then
        if err == "timeout" then
            sock:close()
        end
        return nil, nil, err
    end

    return data, flags
end

local function _expand_table(value)
    local segs = {}
    local nelems = #value
    local nsegs = 0
    for i = 1, nelems do
        local seg = value[i]
        nsegs = nsegs + 1
        if type(seg) == "table" then
            segs[nsegs] = _expand_table(seg)
        else
            segs[nsegs] = seg
        end
    end
    return concat(segs)
end


local function _store(self, cmd, key, value, exptime)
    if not exptime then
        exptime = 0
    end

    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    if type(value) == "table" then
        value = _expand_table(value)
    end

    local req = cmd .. " " .. self.escape_key(key) .. " "
            .. exptime .. " " .. strlen(value) .. "\r\n" .. value
            .. "\r\n"

    local bytes, err = sock:send(req)
    if not bytes then
        return nil, err
    end

    -- discard \r\n
    sock:receive()

    -- get operation result
    local data, err = sock:receive(strlen(value))
    if not data then
        if err == "timeout" then
            sock:close()
        end
        return nil, err
    end

    ngx.log(ngx.DEBUG, "Data : ", data)
    sock:receive()

    -- discard DONE
    local done, err = sock:receive()
    if not done then
        if err == "timeout" then
            sock:close()
        end
        return nil, err
    end

    ngx.log(ngx.DEBUG, "Done : ", done)

    if done == "DONE" then
        return 1
    end

    return nil, data
end


function _M.set(self, ...)
    return _store(self, "set", ...)
end

function _M.set_keepalive(self, ...)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    return sock:setkeepalive(...)
end


function _M.get_reused_times(self)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    return sock:getreusedtimes()
end


local function _incr_decr(self, cmd, key, value)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    local req = cmd .. " " .. self.escape_key(key) .. " " .. value .. "\r\n"

    local bytes, err = sock:send(req)
    if not bytes then
        return nil, err
    end

    local line, err = sock:receive()
    if not line then
        if err == "timeout" then
            sock:close()
        end
        return nil, err
    end

    if not match(line, '^%d+$') then
        return nil, line
    end

    return line
end


function _M.incr(self, key, value)
    return _incr_decr(self, "incr", key, value)
end


function _M.quit(self)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    local bytes, err = sock:send("quit\r\n")
    if not bytes then
        return nil, err
    end

    return 1
end


function _M.touch(self, key, exptime)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    local bytes, err = sock:send("touch " .. self.escape_key(key) .. " "
            .. exptime .. "\r\n")
    if not bytes then
        return nil, err
    end

    local line, err = sock:receive()
    if not line then
        if err == "timeout" then
            sock:close()
        end
        return nil, err
    end

    -- moxi server from couchbase returned stored after touching
    if line == "TOUCHED" or line =="STORED" then
        return 1
    end
    return nil, line
end


function _M.close(self)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    return sock:close()
end


return _M
