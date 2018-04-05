
local escape_uri = ngx.escape_uri
local unescape_uri = ngx.unescape_uri
local match = string.match
local tcp = ngx.socket.tcp
local strlen = string.len
local setmetatable = setmetatable
local tonumber = tonumber
local math = math
local tostring = tostring
local table = table
local ngx = ngx

local _M = {
    _VERSION = '0.99',
    PROTOCOL_ERROR = "protocol_error"
}

local mt = { __index = _M }

local function escape(key)
    local i = 1
    local result = {}
    for m in (tostring(key) .. ":"):gmatch("([^:]*):") do
        result[i] = escape_uri(m)
        i = i + 1
    end
    ngx.log(ngx.DEBUG, "escape result : ", table.concat(result, ":"))
    return table.concat(result, ":")
end

function _M.new(self, opts)
    local sock, err = tcp()
    if not sock then
        return nil, err
    end

    local escape_key = escape
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

local function read_response_line(self, data)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end
    if not data then
        local line, err = sock:receive()
        if not line then
            if err == "timeout" then
                sock:close()
            end
            return nil, err
        end
        ngx.log(ngx.DEBUG, "RECEIVE : ", line)
        local len = match(line, '^LEN (%d+)$')
        if len then
            ngx.log(ngx.DEBUG, "LEN : ", len)
            return "LEN", tonumber(len)
        end
        local done = line == "DONE"
        if done then
            ngx.log(ngx.DEBUG, "DONE")
            return "DONE", nil
        end
        local error = match(line, '^ERROR (%.+)$')
        if error then
            ngx.log(ngx.DEBUG, "ERROR", error)
            return "ERROR", error
        end
        ngx.log(ngx.DEBUG, "PROTOCOL ERROR")
        return nil, _M.PROTOCOL_ERROR
    else
        local data, err = sock:receive(data)
        if not data then
            if err == "timeout" then
                sock:close()
            end
            return nil, err
        end
        ngx.log(ngx.DEBUG, "RECEIVE : ", data)
        -- Discard trailing \r\n
        local trail, err = sock:receive()
        if not trail then
            if err == "timeout" then
                sock:close()
            end
            return nil, err
        end
        ngx.log(ngx.DEBUG, "TRAIL RECIEVED")
        ngx.log(ngx.DEBUG, "DATA : ", data)
        return "DATA", data
    end
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

    local resp, data = read_response_line(self)
    if resp == "LEN" then
        resp, data = read_response_line(self, data)
        if resp == "DATA" then
            local resp, _ = read_response_line(self)
            if resp == "DONE" then
                return data, nil
            else
                return nil, _M.PROTOCOL_ERROR
            end
        else
            return nil, _M.PROTOCOL_ERROR
        end
    elseif resp == "ERROR" then
        if data == "not_found" then
            return nil, "not found"
        else
            return nil, data
        end
    else
        return nil, _M.PROTOCOL_ERROR
    end

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

    local resp, data = read_response_line(self)
    if resp == "DONE" then
        return 1, nil
    elseif resp == "ERROR" then
        return nil, data
    else
        return nil, _M.PROTOCOL_ERROR
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

    local resp, data = read_response_line(self)
    if resp == "LEN" then
        resp, data = read_response_line(self, data)
        if resp == "DATA" then
            local resp, _ = read_response_line(self)
            if resp == "DONE" then
                return data, nil
            else
                return nil, _M.PROTOCOL_ERROR
            end
        else
            return nil, _M.PROTOCOL_ERROR
        end
    elseif resp == "ERROR" then
        return nil, data
    else
        return nil, _M.PROTOCOL_ERROR
    end

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

    local resp, data = read_response_line(self)
    if resp == "LEN" then
        resp, data = read_response_line(self, data)
        if resp == "DATA" then
            local resp, _ = read_response_line(self)
            if resp == "DONE" then
                return data, nil
            else
                return nil, _M.PROTOCOL_ERROR
            end
        else
            return nil, _M.PROTOCOL_ERROR
        end
    elseif resp == "ERROR" then
        return nil, data
    else
        return nil, _M.PROTOCOL_ERROR
    end

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

    local resp, data = read_response_line(self)
    if resp == "DONE" then
        return 1, nil
    elseif resp == "ERROR" then
        return nil, data
    else
        return nil, _M.PROTOCOL_ERROR
    end

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
