package com.company.project.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.company.project.core.Result;
import com.company.project.core.ResultGenerator;
import com.company.project.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class TokenInterceptor implements HandlerInterceptor {

    Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        response.setCharacterEncoding("utf-8");
        String token = request.getHeader("access_token");
        //token不存在
        if (null != token) {
            //验证token是否正确
            boolean result = JwtUtil.verify(token);
            if (result)
                return true;
            else
                logger.error("没有token");
        }
        logger.error("没有token");
        //ApiResponse apiResponse = ApiResponseUtil.getApiResponse(ApiResponseEnum.AUTH_ERROR);
        responseMessage(response,response.getWriter(),ResultGenerator.genFailResult("没有token"));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }
    /**
     * 返回信息给客户端
     *
     * @param response
     * @param out
     * @param result
     */
    private void responseMessage(HttpServletResponse response, PrintWriter out, Result result) {
        response.setContentType("application/json; charset=utf-8");
        out.print(JSONObject.toJSONString(result));
        out.flush();
        out.close();
    }
}