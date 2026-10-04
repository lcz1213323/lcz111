package com.erp.tag;

import com.erp.util.PermissionUtil;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * JSP自定义权限标签
 *
 * 用法：
 * <%@ taglib prefix="perm" uri="http://erp.com/tags" %>
 * <perm:check code="product:add">
 *     <a class="btn" href="add_product.jsp">新增商品</a>
 * </perm:check>
 *
 * 如果当前用户拥有指定权限编码，则输出标签体内容；否则隐藏。
 * 这是按钮级权限控制的核心组件，替代了之前硬编码的 JSTL c:if 角色判断。
 */
public class HasPermissionTag extends TagSupport {

    private String code;  // 权限编码，如 "product:add"

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public int doStartTag() throws JspException {
        HttpSession session = pageContext.getSession();
        if (PermissionUtil.hasPermission(session, code)) {
            return EVAL_BODY_INCLUDE;  // 有权限，输出标签体内容
        }
        return SKIP_BODY;              // 无权限，跳过标签体
    }

    @Override
    public void release() {
        super.release();
        this.code = null;
    }
}