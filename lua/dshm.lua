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
local type = type
local ngx = ngx

local _M = {
    _VERSION = '0.99',
    PROTOCOL_ERROR = "protocol_error"
}

local mt = { __index = _M }

---
---Escape the key
---@param key string the key
---@return string the escaped key
---
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

---
---Constructor
---@param _ self instance
---@param opts table options (optional) in order to override espace_key/unescape_key functions
---@return self instance
---
function _M.new(_, opts)
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
---
---Read response line
---@param self self the dshm instance
---@param data string
---
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
        local err
        data, err = sock:receive(data)
        if not data then
            if err == "timeout" then
                sock:close()
            end
            return nil, err
        end
        ngx.log(ngx.DEBUG, "RECEIVE : ", data)
        -- Discard trailing \r\n
        local trail
        trail, err = sock:receive()
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

---
---Send command
---@param self self instance
---@param command string the command to send
---@param key string the key
---@param params table the command arguments (optional)
---@param data string the data to send (optional)
---@return number number of bytes sent
---@return any error
---
local function send_command(self, command, key, params, data)
    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end
    -- Add args
    local str_arg = ""
    if params then
        if type(params) == "table" then
            str_arg = table.concat(params, " ")
        else
            str_arg = params
        end
        str_arg = " " .. str_arg
    end
    -- Add new line separator
    str_arg = str_arg .. "\r\n"

    -- Add data
    if data then
        str_arg = str_arg .. data
    end

    -- Prepare the full command
    local str_command = command
    if key then
        str_command = str_command .. " " .. self.escape_key(key) .. str_arg
    end
    ngx.log(ngx.DEBUG, "send command to dshm:", str_command)
    local bytes, err = sock:send(str_command)
    if not bytes then
        return nil, err
    end
    return bytes, nil
end

---
---Read and parse response that return DATA
---@param self self the dshm instance
---@return string data
---@return string error
---
local function read_response_data(self)

    local resp, data = read_response_line(self)
    if resp == "LEN" then
        resp, data = read_response_line(self, data)
        if resp == "DATA" then
            resp = read_response_line(self)
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
---
---Execute command get and return the data
---@param self self the dshm instance
---@param key string the key
---@return string data
---
function _M.get(self, key)

    ngx.log(ngx.DEBUG, "Get : ", key)

    local _, err = send_command(self, "get", key)

    if err then
        return nil, err
    end

    local resp
    resp, err = read_response_data(self)
    if err and err == "not_found" then
        err = "not found"
    end
    return resp, err
end

---Delete the key
---@param self self the dshm instance
---@param key string the key to delete
---@return number 1 if key has been deleted or nil
---@return string error
function _M.delete(self, key)

    ngx.log(ngx.DEBUG, "Delete : ", key)

    local _, err = send_command(self, "delete", key)
    if err then
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
---Increment the counter
---@param self self the dshm instance
---@param key string the key
---@param value string the incr value (example : 1, -1, 2)
---@param init string the initial value (optional). If counter doesn't exist, counter is initialized with this value
---@param init_ttl number the initial TTL value (optional). If counter doesn't exist, counter ttl is initialized with this value
---@return string data the counter value after command execution
---@return string error
function _M.incr(self, key, value, init, init_ttl)

    ngx.log(ngx.DEBUG, "Incr : ", key, ", Value : ", value, ", Init : ", init, ", Init_TTL", init_ttl)

    local l_init = init or 0
    local l_init_ttl = init_ttl or 0

    local params = { value, l_init, l_init_ttl }
    local _, err = send_command(self, "incr", key, params)
    if err then
        return nil, err
    end
    return read_response_data(self)

end

---Set a key
---@param self self the dshm instance
---@param key string the key
---@param value string the value
---@param exptime number the TTL value (optional).
---@return string data the value
---@return string error
function _M.set(self, key, value, exptime)

    if exptime and exptime ~= 0 then
        exptime = math.floor(exptime + 0.5)
    end

    ngx.log(ngx.DEBUG, "set Key : ", key, ", Value : ", value, ", Exp : ", exptime)

    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end
    local params = { exptime, strlen(value) }
    local _, err = send_command(self, "set", key, params, value)

    if err then
        return nil, err
    end

    return read_response_data(self)

end
---Touch a key
---@param self self the dshm instance
---@param key string the key
---@param exptime number the new TTL value (optional).
---@return string data the value
---@return string error
function _M.touch(self, key, exptime)

    if exptime and exptime ~= 0 then
        exptime = math.floor(exptime + 0.5)
    end

    ngx.log(ngx.DEBUG, "Touch : ", key, ", Exp : ", exptime)

    local _, err = send_command(self, "touch", key, exptime)
    if err then
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

---
--- Sliding window rate limiter command.
---Rate limiter will try to 'consume' a token and return the remaining tokens available.
---If no tokens were available, this method will return nil, "rejected"
---Otherwise return the next remaining tokens available in the window
---
---@param self self the dshm instance
---@param self string the key
---@param capacity number the tokens capacity
---@param duration number the sliding window duration in seconds
---@return number the remaining tokens available or nil if quota is exceeded
---@return string nil or error. Error code is rejected when quota is exceeded
---
function _M.rate_limiter(self, key, capacity, duration)

    ngx.log(ngx.DEBUG, "rate_limiter : ", key, ", capacity : ", capacity, ", duration : ", duration)

    local sock = self.sock
    if not sock then
        return nil, "not initialized"
    end

    local params = { capacity, duration }
    local _, err = send_command(self, "rate_limiter", key, params)
    if err then
        return nil, err
    end

    local resp
    resp, err = read_response_data(self)
    if resp == "-1" then
        resp = nil
        err = "rejected"
    end
    return resp, err
end
---
---
---Quit command
---@return number 1 when successful
---@return string error
function _M.quit(self)
    local _, err = send_command(self, "quit")
    if err then
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
