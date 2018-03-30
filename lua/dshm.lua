
local escape_uri = ngx.escape_uri
local unescape_uri = ngx.unescape_uri
local match = string.match
local tcp = ngx.socket.tcp
local strlen = string.len
local setmetatable = setmetatable
local ngx = ngx

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


function _M.get(self, key)

    ngx.log(ngx.DEBUG, "Get : ", key)

    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    local bytes, err = sock:send("get " .. self.escape_key(key) .. "\r\n" )
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

    ngx.log(ngx.DEBUG, "Line ", line)

    local len = match(line, '^LEN (%d+)$')
    if not len then
        return nil, nil
    end

    ngx.log(ngx.DEBUG, "LEN ", len)

    local data, err = sock:receive(len)
    if not data then
        if err == "timeout" then
            sock:close()
        end
        return nil, err
    end

    ngx.log(ngx.DEBUG, "Data : ", data)

    if data == "ERROR not_found" then
        return nil, "not found"
    end

    line, err = sock:receive(8) -- discard the trailing "\r\nDONE\r\n"
    if not line then
        if err == "timeout" then
            sock:close()
        end
        return nil, err
    end

    return data, nil
end


function _M.delete(self, key)

    ngx.log(ngx.DEBUG, "Delete : ", key)

    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    local bytes, err = sock:send("delete " .. self.escape_key(key) .. "\r\n" )
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

    ngx.log(ngx.DEBUG, "Line 1 : ", line)


    local line, err = sock:receive()
    if not line then
        if err == "timeout" then
            sock:close()
        end
        return nil, err
    end

    ngx.log(ngx.DEBUG, "Line 2 : ", line)
    if line == "DONE" then
        return 1, nil
    else
        return nil, line
    end

end


function _M.incr(self, key, value, init)

    ngx.log(ngx.DEBUG, "Incr : ", key, ", Value : ", value, ", Init : ", init)

    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    local l_init = 0
    if init then
        l_init = init
    end

    local bytes, err = sock:send("incr " .. self.escape_key(key) .. " " .. value .. " " .. l_init .. "\r\n")
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

    if line == "ERROR not_found" then
        return nil, "not found"
    end

    local len = match(line, '^LEN (%d+)$')
    if not len then
        return nil, nil
    end

    ngx.log(ngx.DEBUG, "LEN ", len)

    local data, err = sock:receive(len)
    if not data then
        if err == "timeout" then
            sock:close()
        end
        return nil, err
    end

    ngx.log(ngx.DEBUG, "Data : ", data)

    line, err = sock:receive(8) -- discard the trailing "\r\nDONE\r\n"
    if not line then
        if err == "timeout" then
            sock:close()
        end
        return nil, err
    end

    return data, nil

end

function _M.set(self, key, value, exptime)

    if not exptime or exptime == 0 then
        exptime = 0
    else
        exptime = math.floor(exptime + 0.5)
    end

    ngx.log(ngx.DEBUG, "Key : ", key, ", Value : ", value, ", Exp : ", exptime)

    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    local req = "set " .. self.escape_key(key) .. " "
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


function _M.touch(self, key, exptime)

    if not exptime or exptime == 0 then
        exptime = 0
    else
        exptime = math.floor(exptime + 0.5)
    end

    ngx.log(ngx.DEBUG, "Touch : ", key, ", Exp : ", exptime)

    local sock = self.sock
    if not sock then
        ngx.log(ngx.DEBUG, "Socket not initialized")
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
            ngx.log(ngx.DEBUG, "Socket timeout")
            sock:close()
        end
        return nil, err
    end

    ngx.log(ngx.DEBUG, "Line 1 : ", line)

    local line, err = sock:receive()
    if not line then
        if err == "timeout" then
            ngx.log(ngx.DEBUG, "Socket timeout")
            sock:close()
        end
        return nil, err
    end

    ngx.log(ngx.DEBUG, "Done : ", line)

    -- moxi server from couchbase returned stored after touching
    if line == "DONE" then
        return 1
    end
    return nil, line
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

function _M.close(self)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    return sock:close()
end


return _M
