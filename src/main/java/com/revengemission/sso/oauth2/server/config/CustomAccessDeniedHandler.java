package com.revengemission.sso.oauth2.server.config;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revengemission.sso.oauth2.server.domain.GlobalConstant;
import com.revengemission.sso.oauth2.server.domain.ResponseResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Value("${access.denied.url:/403}")
    String accessDeniedUrl = "/403";

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response, AccessDeniedException e) throws IOException {
        //服务器地址
        String toUrl = "http://" + request.getServerName()
            + ":"
            + request.getServerPort()           //端口号
            + request.getContextPath()      //项目名称
            + request.getServletPath()      //请求页面或其他地址
            + "?" + (request.getQueryString());
        boolean isAjax = "XMLHttpRequest".equals(request
            .getHeader("X-Requested-With")) || "apiLogin".equals(request
            .getHeader("api-login"));
        if (isAjax) {
            response.setHeader("Content-Type", "application/json;charset=UTF-8");
            try {
                ResponseResult<Object> responseMessage = new ResponseResult<>();
                responseMessage.setStatus(GlobalConstant.ERROR_DENIED);
                responseMessage.setMessage(toUrl);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(response.getOutputStream(),
                    JsonEncoding.UTF8);
                objectMapper.writeValue(jsonGenerator, responseMessage);
            } catch (Exception ex) {
                throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
            }
        } else {
///            response.sendRedirect(accessDeniedUrl + "?toUrl=" + toUrl);
            response.sendRedirect(accessDeniedUrl);
        }
    }
}
