package com.uws.orientation.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.uws.core.base.BaseController;
import com.uws.domain.orientation.StudentGuardianModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.log.Logger;
import com.uws.log.LoggerFactory;
import com.uws.orientation.service.IStudentGuardianService;
import com.uws.orientation.service.IStudentInfoService;

/**
 * 
 * @ClassName: ReportController
 * @Description: TODO(查看学生信息的Controller功能描述：对学生信息的查询查看操作)
 * @author wangcl
 * @date 2015-8-10 上午9:46:48
 * 
 */
@Controller
public class StudentViewController extends BaseController{
	
	// 日志
	private Logger logger = new LoggerFactory(
			StudentViewController.class);
	
	// 学生信息的Service
	@Autowired
	private IStudentInfoService studentInfoService;
			
	//新生监护人的Service
	@Autowired
	private IStudentGuardianService studentGuardianService;
	
	/**
	 * 学生信息弹出页面的查看
	 * 
	 * @param model
	 * @param request
	 * @param Model
	 * @return view
	 */
	@RequestMapping({ "/student/view/nsm/viewStudent" })
	public String viewStudent(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		logger.info("学生信息弹出页面的查看");
		//学生Id
		String id = request.getParameter("id");
		//取得学生基本信息
		StudentInfoModel newStudent = studentInfoService.getStudentInfoById(id);
		//把值带回到页面
		model.addAttribute("newStudent", newStudent);
		//监护人信息
		StudentGuardianModel sgm= studentGuardianService.getByStudentId(id);		
		//把值带回到页面
		model.addAttribute("guardian", sgm);
		//返回到的页面
		return "orientation/common/studentView";
	}
}
