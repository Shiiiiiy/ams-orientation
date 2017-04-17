package com.uws.orientation.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.uws.core.base.BaseController;
import com.uws.core.excel.ExcelException;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.log.Logger;
import com.uws.log.LoggerFactory;
import com.uws.orientation.service.IStudentInfoService;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.IDicService;
import com.uws.sys.service.IFileService;
import com.uws.sys.service.impl.DicFactory;
import com.uws.sys.service.impl.FileFactory;
import com.uws.sys.util.Constants;
import com.uws.sys.util.MultipartFileValidator;
import com.uws.user.model.User;

/**
 * 
 * @ClassName: ReportController
 * @Description: TODO(迎新报到信息导入办理的Controller功能描述：对迎新报到数据的导入等操作)
 * @author wangcl
 * @date 2015-8-01 上午9:46:48
 * 
 */
@Controller
public class OffLineReportController extends BaseController{
	
	// 日志
	private Logger logger = new LoggerFactory(
			OffLineReportController.class);
	
	// 学生信息的Service
	@Autowired
	private IStudentInfoService studentInfoService;
	
	@Autowired
	protected IFileService fileService;
	
	@Autowired
	private DicUtil dicUtil = DicFactory.getDicUtil();
	
	@Autowired
	private IDicService dicService;
	//private SessionUtil sessionUtil = SessionFactory.getSession("/edus/student");
	
	// 文件工具类
	private FileUtil fileUtil = FileFactory.getFileUtil();
	
	/**
	 * 导入初始化
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/off/line/opt-query/importInit")
	public String importInit(HttpServletRequest request, ModelMap model) {
		
		logger.info("报到信息导入初始化处理");
		return "/orientation/scene/offline/importReport";
	}
	
	/**
	 * 导入学生报到信息
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "finally" })
	@RequestMapping(value = "/off/line/opt-query/importReport")
	public String importStudent(ModelMap model,
			@RequestParam("file") MultipartFile file, String maxSize,
			String allowedExt, HttpServletRequest request) {
		
		logger.info("学生报到信息的导入处理");
		//错误信息
		String errorText = "";
		// 构建文件验证对象
		MultipartFileValidator validator = new MultipartFileValidator();
		
		if (org.apache.commons.lang.StringUtils.isNotEmpty(allowedExt))
			validator.setAllowedExtStr(allowedExt.toLowerCase());
		
		//设置文件大小
		if (org.apache.commons.lang.StringUtils.isNotEmpty(maxSize)) {
			validator.setMaxSize(Long.valueOf(maxSize));// 20M
		} else {
			validator.setMaxSize(1024 * 1024 * 20);// 20M
			
		}
		
		//调用验证框架自动验证数据
		String returnValue = validator.validate(file);
		
		if (!returnValue.equals("")) {
			
			model.addAttribute("errorText", returnValue);
			return "/orientation/scene/offline/importReport";
		}

		String tempFileId = fileUtil.saveSingleFile(true, file);
		System.out.println("tempFileId:" + tempFileId);
		
		File tempFile = fileUtil.getTempRealFile(tempFileId);
		
		try {
			Map map = new HashMap();
			
			//获取当前登录用户
			SessionUtil sessionUtil = SessionFactory.getSession(Constants.MENUKEY_SYSCONFIG);
			String userId = sessionUtil.getCurrentUserId();
			User userNew = new User();
			userNew.setId(userId);
		    //map.put("temp.creator", userNew);
		    //map.put("temp.statusDic", dicUtil.getDicInfo("STU_STATUS ", "STU_STATUS_01"));
		    //map.put("temp.submitStateDic", dicUtil.getDicInfo("SUBMIT_STATUS ", "NO_SUBMIT"));

			// 把Excel文件中的导入数据，保存到数据库中。
			/*String message = this.studentService.importData(tempFile
					.getAbsolutePath(), "studentImport", map, Student.class);*/
		    String message = this.studentInfoService.importData(tempFile
			.getAbsolutePath(), "importReport", map, StudentInfoModel.class);
		    
			
			if (message != null && !"".equals(message)) {
				errorText = message;
			}
			
		} catch (ExcelException e) { 
			errorText = e.getMessage();
		} catch (InstantiationException e) {
			e.printStackTrace();
			// errorText = "InstantiationException="+e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			// errorText = "IOException="+e.getMessage();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			// errorText = "IllegalAccessException="+e.getMessage();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			// errorText = "ClassNotFoundException="+e.getMessage();
		} catch (Exception e) {
			 e.printStackTrace();
			errorText = "请上传正确模板的Excel文件！";
		} finally {
			
			model.addAttribute("errorText", errorText);
			return "/orientation/scene/offline/importReport";
		}

	}
	
}
