package com.uws.orientation.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.uws.core.base.BaseController;
import com.uws.core.util.DataUtil;
import com.uws.core.util.IdUtil;
import com.uws.log.Logger;
import com.uws.log.LoggerFactory;
import com.uws.orientation.service.IStudentInfoService;
import com.uws.security.exception.CodeInvalidException;
import com.uws.security.exception.PasswordInvalidException;

/**
 * 
* @ClassName: NewStudentLoginController 
* @Description: TODO(新生登录信息采集系统的Controller) 
* @author wangcl
* @date 2015-7-23 上午9:46:48 
*
 */
@Controller
public class NewStudentLoginController extends BaseController {

	private Logger log = new LoggerFactory(NewStudentLoginController.class);
	
	public static final String CODE_INVALID = "验证码错误";

	public static final String USERNAME_PASSWORD_INVALID = "证件号或者密码错误";
	
	//public static final String AUDIT_INVALID = "学生信息未审核通过，暂不能登录";
	
	//学生基本信息的Service
	@Autowired
	private IStudentInfoService studentInfoService;

	
	/**
	 * 
	* @Title: loginUI（新生登录的初始页面） 
	* @Description: TODO(新生登录的初始页面)
	* @param response 
	* @param request
	* @param model
	* @return String
	* @author wangcl
	 */
	@RequestMapping({ "/newstudent/login" })
	public String loginUI(HttpSession session, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		Object userObject = session.getAttribute("student_key");
		if (DataUtil.isNotNull(userObject)) {
			return "orientation/login/toMainUI";
		}

		model.addAttribute("coderandom", IdUtil.getUUIDHEXStr());
		
		String errorType = request.getParameter("errorType");
		if (errorType != null) {
			if (errorType.equals("USERNAME_PASSWORD_INVALID"))
				model.addAttribute("errorMessage", USERNAME_PASSWORD_INVALID);
			else if (errorType.equals("CODE_INVALID")) {
				model.addAttribute("errorMessage", CODE_INVALID);
			}
			else if (errorType.equals("AUDIT_INVALID")) {
				//model.addAttribute("errorMessage", AUDIT_INVALID);
				model.addAttribute("errorMessage", USERNAME_PASSWORD_INVALID);
			}
		}
		return "orientation/login/loginUI";
	}

	/**
	 * 
	* @Title: loginAuth 
	* @Description: TODO(新生登录处理)
	* @param certificateCode 证件号
	* @param passWord 密码
	* @param response 
	* @param request
	* @param model
	* @return String
	* @author wangcl
	 */
	@RequestMapping(value = { "/newstudent/loginauth" }, method = { RequestMethod.POST })
	public String loginAuth(@RequestParam String certificateCode,@RequestParam String passWord,@RequestParam String code, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		try {
			studentInfoService.loginAuth(certificateCode, passWord,code);
		} catch (PasswordInvalidException e) {
			this.log.error(e.getMessage());
			return "redirect:/newstudent/login.do?errorType=USERNAME_PASSWORD_INVALID";
		} catch (CodeInvalidException e) {
			this.log.error(e.getMessage());
			return "redirect:/newstudent/login.do?errorType=CODE_INVALID";
		} catch (Exception e) {
			this.log.error(e.getMessage());
			return "redirect:/newstudent/login.do?errorType=AUDIT_INVALID";
		}
		//return "redirect:/newstudent/main.do";
		return "redirect:/newstudent/register/viewWriteComm.do";
	}

	/**
	 * 
	* @Title: main 
	* @Description: TODO(新生登录成功的主页)
	* @param response 
	* @param request
	* @param model
	* @return String
	* @author wangcl
	 */
	@RequestMapping({ "/newstudent/main" })
	public String main(HttpServletRequest request, HttpServletResponse response) {
		return "orientation/main/main";
	}
	
	/**
	 * 
	* @Title: toLoginUI 
	* @Description: TODO(新生登录成功的主页)
	* @param response 
	* @param request
	* @return String
	* @author wangcl
	 */
	@RequestMapping({ "/newstudent/toLogin" })
	public String toLoginUI(HttpServletRequest request,
			HttpServletResponse response, Model model) {
		return "orientation/login/toLoginUI";
	}

	/**
	 * 
	* @Title: logout 
	* @Description: TODO(新生登录后注销的处理)
	* @param response 
	* @param request
	* @return String
	* @author wangcl
	 */
	@RequestMapping({ "/newstudent/logout" })
	public String logout(HttpSession session, HttpServletResponse response,
			HttpServletRequest request) {
		Object newStudent = session.getAttribute("student_key");
		if (DataUtil.isNotNull(newStudent)) {
			session.invalidate();
		}
		return "redirect:/newstudent/login.do";
	}
}
