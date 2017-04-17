package com.uws.orientation.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.uws.common.service.IBaseDataService;
import com.uws.common.util.AmsDateUtil;
import com.uws.common.util.SchoolYearUtil;
import com.uws.comp.service.ICompService;
import com.uws.core.base.BaseController;
import com.uws.core.excel.ExcelException;
import com.uws.core.excel.service.IExcelService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.DateUtil;
import com.uws.core.util.StringUtils;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseMajorModel;
import com.uws.domain.base.BaseRoomModel;
import com.uws.domain.base.StudentRoomModel;
import com.uws.domain.orientation.StudentGuardianModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.orientation.WelcomeSetModel;
import com.uws.log.Logger;
import com.uws.log.LoggerFactory;
import com.uws.orientation.service.IStudentGuardianService;
import com.uws.orientation.service.IStudentInfoService;
import com.uws.orientation.service.IWelcomeSetService;
import com.uws.orientation.util.Constants;
import com.uws.sys.model.Dic;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.util.ProjectSessionUtils;

/**
 * 
 * @ClassName: ReportController
 * @Description: TODO(迎新报到办理的Controller功能描述：对迎新报到数据的修改、查询等操作)
 * @author wangcl
 * @date 2015-8-01 上午9:46:48
 * 
 */
@Controller
public class ReportController extends BaseController {

	// 日志
	private Logger logger = new LoggerFactory(ReportController.class);

	// 学生信息的Service
	@Autowired
	private IStudentInfoService studentInfoService;

	// 新生监护人的Service
	@Autowired
	private IStudentGuardianService studentGuardianService;

	// 学院、专业、班级的查询
	@Autowired
	private ICompService compService;

	// 基础信息查询
	@Autowired
	private IBaseDataService baseDataService;

	// 迎新数据设置信息的Service
	@Autowired
	private IWelcomeSetService welcomeSetService;

	// 导出Excel的Service
	@Autowired
	private IExcelService excelService;

	// session的共通操作
	private SessionUtil sessionUtil = SessionFactory
			.getSession(Constants.SCENE_REPORT);

	// session的常量查询的Po
	private final static String SCENE_REPORT = "SCENEREPORT";

	// session的常量页数
	private final static String SCENE_REPORT_PAGENO = "SCENEREPORTPAGENO";

	// 数据字典帮助对象
	private DicUtil dicUtil = DicFactory.getDicUtil();

	/**
	 * 新生信息报到查询
	 * 
	 * @param model
	 * @param request
	 * @param testPo
	 * @return
	 */
	@RequestMapping({ Constants.SCENE_REPORT + "/opt-query/sceneReportList" })
	public String scenReportList(ModelMap model, HttpServletRequest request,
			StudentInfoModel po, RedirectAttributes attr) {
		logger.info("新生现场报到查询处理");

		String collegeId = "";
		// 判断登录人是不是教职工
		if (ProjectSessionUtils.checkIsTeacher(request)) {
			// 取得登录用户的部门Id
			String orgId = ProjectSessionUtils.getCurrentTeacherOrgId(request);
			// 根据学院Id取得学院信息
			BaseAcademyModel college = baseDataService.findAcademyById(orgId);
			if (college != null) {
				// 学院当成查询条件
				po.setCollege(college);
				collegeId = college.getId();
			}
		}
		// 默认的学院Id
		model.addAttribute("collegeId", collegeId);

		// 对应学年
		po.setEnterYearDic(SchoolYearUtil.getYearDic());

		// 分页
		int pageNo = request.getParameter("pageNo") != null ? Integer
				.valueOf(request.getParameter("pageNo")) : 1;
		// 判断是否是从查看页面返回
		String backFlag = request.getParameter(Constants.BACK_FLAG);
		if (!org.apache.commons.lang.StringUtils.isBlank(backFlag)) {
			// 如果是从查看页面返回的则学生信息查询页面中的信息应该从session中取得。

			StudentInfoModel epp = (StudentInfoModel) sessionUtil
					.getSessionAttribute(SCENE_REPORT);
			Integer pageNoSession = (Integer) sessionUtil
					.getSessionAttribute(SCENE_REPORT_PAGENO);
			if (epp != null && pageNoSession != null) {
				this.redirectAttribute(attr, pageNoSession.toString(), epp);
				pageNo = pageNoSession;
				po = epp;
			}
		} else {
			// 把查询的信息保存到session中。
			sessionUtil.setSessionAttribute(SCENE_REPORT, po);
			// 分页中当前页面的页数保存。
			sessionUtil.setSessionAttribute(SCENE_REPORT_PAGENO, pageNo);
		}

		Page page = studentInfoService.pageQueryStudentReport(
				Page.DEFAULT_PAGE_SIZE, pageNo, po);

		if (page.getTotalPageCount() == 0 && pageNo > 1) {
			page = studentInfoService.pageQueryStudentReport(
					Page.DEFAULT_PAGE_SIZE, pageNo - 1, po);

		}
		model.addAttribute("page", page);
		// 学院
		List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		// 专业
		List<BaseMajorModel> majorList = null;
		// 班级
		List<BaseClassModel> classList = null;
		if (null != po && null != po.getMajor() && null != po.getCollege()
				&& po.getCollege().getId().length() > 0) {
			majorList = compService
					.queryMajorByCollage(po.getCollege().getId());
			logger.debug("若已经选择学院，则查询学院下的专业信息.");

			classList = compService.queryClassByMajor(po.getMajor().getId());
			logger.debug("若已经选择专业，则查询学院下的班级信息.");
		}

		model.addAttribute("page", page);
		model.addAttribute("collegeList", collegeList);
		model.addAttribute("majorList", majorList);
		model.addAttribute("classList", classList);

		// 报到地点
		List<Dic> siteList = dicUtil.getDicInfoList("REPORT_SITE");
		model.addAttribute("siteList", siteList);

		// 报到状态List
		model.addAttribute("sceneReportList", Constants.SCENE_REPORT_STATE_LIST);

		// 绿色通道的状态List
		model.addAttribute("greenWayList", Constants.GREEN_CHANNEL_STATE_LIST);

		// 设置学生信息
		model.addAttribute("studentInfo", po);
		
		// 判断是从报到成功的返回
		String reportStuId = request.getParameter(Constants.REPORT_STU_ID);
		if (!org.apache.commons.lang.StringUtils.isBlank(reportStuId)) {
			// 设置学生报到成功
			model.addAttribute("reportStuId", reportStuId);
		}else{
			// 设置学生报到成功
			model.addAttribute("reportStuId", "");
		}

		// 页面跳转到报到办理查询页面
		return Constants.SCENE_REPORT_FTL + "reportList";
	}

	/**
	 * 新生刷身份证弹出页面
	 * 
	 * @param model
	 * @param request
	 * @param Model
	 * @return view
	 */
	@RequestMapping({ "/student/view/nsm/newStudentReport" })
	public String viewStudent(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		logger.info("新生刷身份证弹出页面");
		// 身份证号码
		String cer = request.getParameter("cer");
		// 取得学生基本信息
		StudentInfoModel newStudent = studentInfoService
				.getByCertificatCodeAndYear(cer);
		
		StudentRoomModel room = baseDataService.findRoomByStudentId(newStudent.getId());
		String roomStr="";
		if(room!=null){
			if(room.getRoom() !=null && room.getRoom().getBuilding()!=null){
				BaseRoomModel broom = room.getRoom();
			roomStr+=roomStr+broom.getName()+"(+"+broom.getBuilding().getName()+broom.getFloorNum()+broom.getRoomNum()+broom.getRoomorder()+"+)";
			}	
		}
		newStudent.setDorm(roomStr);
		
		// 把值带回到页面
		model.addAttribute("newStudent", newStudent);

		// 返回到的页面
		return "orientation/common/certificatReport";
	}

	/**
	 * 更新报到的状态
	 * 
	 * @param id
	 *            新生的Id
	 * @param reportSiteVal
	 *            报到点
	 * @param statusReport
	 *            报到状态
	 * 
	 * @param NewStudent
	 *            新生的Po
	 * @param attr
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { Constants.SCENE_REPORT
			+ "/opt-update/reportStatus" })
	public String updateReportStatus(@RequestParam("id") String id,
			@RequestParam("reportSiteVal") String reportSiteVal,
			@RequestParam("statusReport") String statusReport,
			StudentInfoModel student, RedirectAttributes attr,
			HttpServletRequest request,ModelMap model) {
		logger.info("新报到的状态");
		//报到成功的学生Id
		String studentId ="";
		Dic siteT = null;
		// 更新为提交状态
		// sceneService.updateStatus(id, "1", value);
		if ("1".equals(statusReport)) {

			if (!"".equals(reportSiteVal)) {
				// 报到点List
				List<Dic> siteDicList = dicUtil.getDicInfoList("REPORT_SITE");
				for (Dic siteTemp : siteDicList) {
					if (reportSiteVal.equals(siteTemp.getId())) {
						siteT = siteTemp;
						break;
					}
				}
				// 更新为报到状态
				studentInfoService.updateStudentReportStatus(id, "1", siteT,
						null);
			} else {
				// 更新为报到状态
				studentInfoService.updateStudentReportStatus(id, "1", null,
						null);
			}
			
			//报到成功的学生Id
			studentId =id;
			
		} else if ("0".equals(statusReport)) {
			// 取消报到
			studentInfoService.updateStudentReportStatus(id, "0", null, null);
			
			//报到成功的学生Id
			studentId ="";

		}
		String pageNo = request.getParameter("pageNo");
		this.redirectAttribute(attr, pageNo, student);
		return "redirect:" + Constants.SCENE_REPORT
				+ "/opt-query/sceneReportList.do?reportStuId="+studentId;
	}

	/**
	 * 刷身份证取消报到办理
	 * 
	 * @param id
	 *            id
	 * @param response
	 * @return String 
	 */
	@RequestMapping(value = { Constants.SCENE_REPORT
				+ "/opt-query/unCerReport" }, produces = { "text/plain;charset=UTF-8" })
		@ResponseBody
		public String unCerReport(HttpServletResponse response, String id) {
			logger.info("刷身份证取消报到办理");
			// 更新为报到状态
			if(id !=null && !"".equals(id)){
				// 更新为报到状态
				// 取消报到
				studentInfoService.updateStudentReportStatus(id, "0", null, null);
				return  "success";
			}else{
				
				return "false";
			}
			
		}
	
	/**
	 * 刷身份证报到办理
	 * 
	 * @param id
	 *            id
	 * @param response
	 * @return String 
	 */
	@RequestMapping(value = { Constants.SCENE_REPORT
				+ "/opt-query/cerReport" }, produces = { "text/plain;charset=UTF-8" })
		@ResponseBody
		public String cerReport(HttpServletResponse response, String id) {
			logger.info("刷身份证报到办理");
			// 更新为报到状态
			if(id !=null && !"".equals(id)){
				// 更新为报到状态
				studentInfoService.updateStudentReportStatus(id, "1", null, null);
				
				// 取得学生基本信息
				StudentInfoModel newStudent = studentInfoService.getStudentInfoById(id);
				Date reportDate = newStudent.getReportDate();
				String reportDateStr ="";
				if(reportDate !=null ){
					reportDateStr = AmsDateUtil.getCustomDateString(reportDate, "yyyy-MM-dd HH:mm:ss");
					return  "success;"+reportDateStr;
				}else{
					return  "success;"+"";
				}
				
			}else{
				
				return "false;";
			}
			
		}

	/**
	 * 重置密码
	 * 
	 * @param id
	 *            新生的Id
	 * @param NewStudent
	 *            新生的Po
	 * @param attr
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { Constants.SCENE_REPORT
			+ "/opt-update/resetPassword" }, produces = { "text/plain;charset=UTF-8" })
	@ResponseBody
	public String resetPassword(HttpServletResponse response,
			@RequestParam("id") String id) {
		logger.info("重置密码操作");
		// 更新为提交状态
		// 学生基本信息
		StudentInfoModel student = studentInfoService.getStudentInfoById(id);
		// 证件号码
		String cerCode = student.getCertificateCode();
		// 重置的密码
		String newPassWord = "";
		if (!org.apache.commons.lang.StringUtils.isBlank(cerCode)
				&& cerCode.length() >= 6) {
			// 重置的密码是证件号的后六位。
			newPassWord = cerCode.substring(cerCode.length() - 6);
		} else {
			// 当证件号码有误时的默认密码
			newPassWord = "123456";
		}

		try {
			studentInfoService.updateNewStudentPassWord(id, newPassWord);
			// 返回的提示信息
			// return "新生："+student.getName()+"信息采集登录密码重置成功！";
			return "success";
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// 返回的提示信息
			// return "新生："+student.getName()+"信息采集登录密码重置失败！";
			return "fail";
		}

	}

	/**
	 * 打印报到流程单
	 * 
	 * @param model
	 * @param request
	 * @param StudentInfoModel
	 *            po
	 * @return
	 */
	@RequestMapping({ Constants.SCENE_REPORT + "/opt-print/nsm/printReport" })
	public String printReport(ModelMap model, HttpServletRequest request,
			StudentInfoModel po, RedirectAttributes attr, String flagReport,
			HttpServletResponse response) {
		logger.info("打印报到流程单");
		// 新生的记录号
		String id = request.getParameter("id");
		// 学生基本信息
		StudentInfoModel student = studentInfoService.getStudentInfoById(id);

		// 取得迎新设置信息
		WelcomeSetModel wsm = welcomeSetService.getWelcomeSet();
		if (wsm == null) {
			wsm = new WelcomeSetModel();
		}

		// 当前学年
		String year = DateUtil.getCurDate();

		// 当前学年
		model.addAttribute("year", year.substring(0, 4));

		// 学院
		if (student != null && student.getCollege() != null) {
			model.addAttribute("collegeName", student.getCollege().getName());
		} else {
			model.addAttribute("collegeName", "");
		}
		// 专业
		if (student != null && student.getMajor() != null) {
			model.addAttribute("majorName", student.getMajor().getMajorName());
		} else {
			model.addAttribute("majorName", "");
		}

		// 班级
		if (student != null && student.getClassId() != null) {
			model.addAttribute("className", student.getClassId().getClassName());
		} else {
			model.addAttribute("className", "");
		}

		// 宿舍
		if (student != null) {
			model.addAttribute("dormName", student.getDorm());
		} else {
			model.addAttribute("dormName", "");
		}

		// 姓名
		if (student != null) {
			model.addAttribute("name", student.getName());
		} else {
			model.addAttribute("name", "");
		}

		// 流程信息
		model.addAttribute("reportComm", wsm.getReportComm());

		// 设置学生信息
		// model.addAttribute("studentInfo", student);

		// 页面跳转到考试安排的查询页面
		return Constants.SCENE_REPORT_FTL + "printReport";
	}

	/**
	 * 操作完成后重定向值传递
	 * 
	 * @param attr
	 * @param pageNo
	 * @param studentInfoModel
	 */
	public void redirectAttribute(RedirectAttributes attr, String pageNo,
			StudentInfoModel stuModel) {
		logger.info("新生报到后时，操作完成后重定向值传递");
		// 页数
		if (StringUtils.hasText(pageNo)) {
			attr.addAttribute("pageNo", pageNo);
		}

		// 学年
		/*
		 * if (examPaperPo.getYearDic() != null &&
		 * StringUtils.hasText(examPaperPo.getYearDic().getId())) {
		 * attr.addAttribute("yearDic.id", examPaperPo.getYearDic().getId()); }
		 */

		// 学院
		if (stuModel.getCollege() != null
				&& !org.apache.commons.lang.StringUtils.isBlank(stuModel
						.getCollege().getId())) {
			attr.addAttribute("college.id", stuModel.getCollege().getId());
		}

		// 专业
		if (stuModel.getMajor() != null
				&& !org.apache.commons.lang.StringUtils.isBlank(stuModel
						.getMajor().getId())) {
			attr.addAttribute("major.id", stuModel.getMajor().getId());
		}

		// 班级
		if (stuModel.getClassId() != null
				&& !org.apache.commons.lang.StringUtils.isBlank(stuModel
						.getClassId().getId())) {
			attr.addAttribute("classId.id", stuModel.getClassId().getId());
		}

		// 姓名
		if (!org.apache.commons.lang.StringUtils.isBlank(stuModel.getName())) {
			attr.addAttribute("name", stuModel.getName());
		}

		// 学号
		if (!org.apache.commons.lang.StringUtils.isBlank(stuModel
				.getStuNumber())) {
			attr.addAttribute("stuNumber", stuModel.getStuNumber());
		}

		// 身份证号
		if (!org.apache.commons.lang.StringUtils.isBlank(stuModel
				.getCertificateCode())) {
			attr.addAttribute("certificateCode", stuModel.getCertificateCode());
		}

		// 报到状态
		if (!org.apache.commons.lang.StringUtils.isBlank(stuModel.getStatus())) {
			attr.addAttribute("status", stuModel.getStatus());
		}

		// 绿色通道状态
		if (!org.apache.commons.lang.StringUtils
				.isBlank(stuModel.getGreenWay())) {
			attr.addAttribute("greenWay", stuModel.getGreenWay());
		}

		// studentInfo
		// attr.addAttribute("studentInfo", stuModel);

	}

	/**
	 * 导出学生信息
	 * 
	 * @param model
	 * @param request
	 * @param user
	 * @param response
	 */
	@RequestMapping({ Constants.SCENE_REPORT + "/opt-query/exportStudentExcel" })
	public void exportStudentExcel(ModelMap model, HttpServletRequest request,
			StudentInfoModel student, HttpServletResponse response) {
		// String exportSize=request.getParameter("demoPoQuery_exportSize");
		// String exportPage=request.getParameter("demoPoQuery_exportPage");
		// String exportSize="1000";
		// String exportPage="1";
		// appBiblPo.setAuthor(request.getParameter("author"));
		// appBiblPo.setTitle(request.getParameter("title"));
		// appBiblPo.setIsbn(request.getParameter("isbn"));
		// String leaveP = request.getParameter("leaveProject");//离校项目
		// String attnId = request.getParameter("attnId");//经办人ID
		// User user = new User();
		// user.setId(attnId);
		// leaveProject.setAttnId(user);
		// leaveProject.setLeaveProject(leaveP);

		// Page page= leaveProjectService.pagedQueryPo(
		// Integer.parseInt(exportPage),Integer.parseInt(exportSize),leaveProject,null);

		// Page page = this.userService.queryUserList(user,
		// Integer.parseInt(exportPage), Integer.parseInt(exportSize));
		List<Map> listMap = new ArrayList<Map>();

		// 入学年份
		// String enterYear= DateUtils.format(new Date(), "yyyy");
		// 对应学年
		student.setEnterYearDic(SchoolYearUtil.getYearDic());

		// student.setEnterYear(DateUtils.format(new Date(), "yyyy"));

		// List<LeaveProject> demoList = (List<LeaveProject>) page.getResult();
		// System.out.println("demoList.size():"+demoList.size());
		List<StudentInfoModel> studentList = studentInfoService
				.getStudentInfoByReport(student);

		for (int i = 0; i < studentList.size(); i++) {
			Map<String, Object> newmap = new HashMap<String, Object>();
			// 序号
			newmap.put("indexs", String.valueOf(i + 1));
			// 学生信息
			StudentInfoModel sim = studentList.get(i);

			// 学院
			if (sim.getCollege() != null) {
				newmap.put("college", sim.getCollege().getName());
			} else {
				newmap.put("college", "");
			}

			// 专业
			if (sim.getMajor() != null) {
				newmap.put("major", sim.getMajor().getMajorName());
			} else {
				newmap.put("major", "");
			}

			// 班级
			if (sim.getClassId() != null) {
				newmap.put("className", sim.getClassId().getClassName());
			} else {
				newmap.put("className", "");
			}
			// 宿舍
			if (sim.getDorm() != null) {
				newmap.put("dormName", sim.getDorm());
			} else {
				newmap.put("dormName", "");
			}

			// 学号
			if (sim.getStuNumber() != null) {
				newmap.put("stuNumber", sim.getStuNumber());
			} else {
				newmap.put("stuNumber", "");
			}

			// 学号
			if (sim.getStuNumber() != null) {
				newmap.put("stuNumber", sim.getStuNumber());
			} else {
				newmap.put("stuNumber", "");
			}

			// 姓名
			if (sim.getName() != null) {
				newmap.put("name", sim.getName());
			} else {
				newmap.put("name", "");
			}

			// 汉语拼音
			if (sim.getNamePy() != null) {
				newmap.put("namePy", sim.getNamePy());
			} else {
				newmap.put("namePy", "");
			}

			// 英文名
			if (sim.getEnglishName() != null) {
				newmap.put("englishName", sim.getEnglishName());
			} else {
				newmap.put("englishName", "");
			}

			// 曾用名
			if (sim.getOldName() != null) {
				newmap.put("oldName", sim.getOldName());
			} else {
				newmap.put("oldName", "");
			}

			// 性别
			if (sim.getGenderDic() != null) {
				newmap.put("sex", sim.getGenderDic().getName());
			} else {
				newmap.put("sex", "");
			}

			// 出生日期
			if (sim.getBrithDate() != null) {
				newmap.put("birthDate",
						AmsDateUtil.getCustomDateString(sim.getBrithDate(), "yyyy-MM-dd"));
				
			} else {
				newmap.put("birthDate", "");
			}

			// 证件证号
			if (sim.getCertificateCode() != null) {
				newmap.put("cerCode", sim.getCertificateCode());
			} else {
				newmap.put("cerCode", "");
			}

			// 政治面貌
			if (sim.getPoliticalDic() != null) {
				newmap.put("political", sim.getPoliticalDic().getName());
			} else {
				newmap.put("political", "");
			}

			// 民族
			if (sim.getNational() != null) {
				newmap.put("nation", sim.getNational());
			} else {
				newmap.put("nation", "");
			}

			// 毕业学校
			if (sim.getGraduation() != null) {
				newmap.put("graduation", sim.getGraduation());
			} else {
				newmap.put("graduation", "");
			}

			// 录取总分
			if (sim.getEnterScore() != null) {
				newmap.put("enterScore", sim.getEnterScore().toString());
			} else {
				newmap.put("enterScore", "");
			}

			// 生源地
			if (sim.getSourceLand() != null) {
				newmap.put("sorceLand", dicUtil.getDicInfo("NATIVE",sim.getSourceLand()));
				//newmap.put("sorceLand", sim.getSourceLand());
			} else {
				newmap.put("sorceLand", "");
			}

			// 籍贯
			if (sim.getNativeDic() != null) {
				newmap.put("native", sim.getNativeDic().getName());
			} else {
				newmap.put("native", "");
			}

			// 户口类别
			if (sim.getAddressTypeDic() != null) {
				newmap.put("addressType", sim.getAddressTypeDic().getName());
			} else {
				newmap.put("addressType", "");
			}

			// 户口地址
			if (sim.getNativeAdd() != null) {
				newmap.put("nativeAddress", sim.getNativeAdd());
			} else {
				newmap.put("nativeAddress", "");
			}

			// 家庭地址
			if (sim.getHomeAddress() != null) {
				newmap.put("homeAddress", sim.getHomeAddress());
			} else {
				newmap.put("homeAddress", "");
			}

			// 家庭邮政编码
			if (sim.getHomePostCode() != null) {
				newmap.put("homePostCode", sim.getHomePostCode());
			} else {
				newmap.put("homePostCode", "");
			}

			// 家庭电话
			if (sim.getHomeTel() != null) {
				newmap.put("homeTel", sim.getHomeTel());
			} else {
				newmap.put("homeTel", "");
			}

			// 手机号码1
			if (sim.getPhone1() != null) {
				newmap.put("phone1", sim.getPhone1());
			} else {
				newmap.put("phone1", "");
			}

			// 手机号码2
			if (sim.getPhone2() != null) {
				newmap.put("phone2", sim.getPhone2());
			} else {
				newmap.put("phone2", "");
			}

			// 电子邮箱
			if (sim.getEmail() != null) {
				newmap.put("email", sim.getEmail());
			} else {
				newmap.put("email", "");
			}

			// 网络地址
			if (sim.getUrlStr() != null) {
				newmap.put("url", sim.getUrlStr());
			} else {
				newmap.put("url", "");
			}

			// 婚姻状况
			if (sim.getMarriageDic() != null) {
				newmap.put("marriage", sim.getMarriageDic().getName());
			} else {
				newmap.put("marriage", "");
			}

			// 港澳台侨
			if (sim.getOverChineseDic() != null) {
				newmap.put("overChinese", sim.getOverChineseDic().getName());
			} else {
				newmap.put("overChinese", "");
			}

			// 宗教信仰
			if (sim.getReligionDic() != null) {
				newmap.put("religion", sim.getReligionDic().getName());
			} else {
				newmap.put("religion", "");
			}

			// 健康状况
			if (sim.getHealthStateDic() != null) {
				newmap.put("health", sim.getHealthStateDic().getName());
			} else {
				newmap.put("health", "");
			}

			// 血型
			if (sim.getBloodTypeDic() != null) {
				newmap.put("bloodType", sim.getBloodTypeDic().getName());
			} else {
				newmap.put("bloodType", "");
			}

			// QQ
			if (sim.getQq() != null) {
				newmap.put("QQ", sim.getQq());
			} else {
				newmap.put("QQ", "");
			}

			// 银行卡号
			if (sim.getBankCode() != null) {
				newmap.put("bankCode", sim.getBankCode());
			} else {
				newmap.put("bankCode", "");
			}

			// 入党申请
			if (sim.getPartyApp() != null) {
				if ("1".equals(sim.getPartyApp())) {
					newmap.put("partyApp", "已申请");
				} else {
					newmap.put("partyApp", "未申请");
				}
			} else {
				newmap.put("partyApp", "");
			}

			// 党校学习
			if (sim.getPartyStudy() != null) {
				if ("1".equals(sim.getPartyStudy())) {
					newmap.put("partyStudy", "已学习");
				} else {
					newmap.put("partyStudy", "未学习");
				}
			} else {
				newmap.put("partyStudy", "");
			}

			// 缴费状况
			if (sim.getCostState() != null) {
				if ("1".equals(sim.getCostState())) {
					newmap.put("costState", "已缴");
				} else {
					newmap.put("costState", "未缴");
				}
			} else {
				newmap.put("costState", "");
			}

			// 绿色通道
			if (sim.getGreenWay() != null) {
				if ("1".equals(sim.getGreenWay())) {
					newmap.put("greenWay", "是");
				} else {
					newmap.put("greenWay", "否");
				}
			} else {
				newmap.put("greenWay", "");
			}

			// 绿色通道原因
			if (sim.getGreenReason() != null) {
				newmap.put("greenReason", sim.getGreenReason().getName());
			} else {
				newmap.put("greenReason", "");
			}

			// 状态
			if (sim.getStatus() != null) {
				if ("1".equals(sim.getStatus())) {
					newmap.put("status", "已报到");
				} else if ("2".equals(sim.getStatus())) {
					newmap.put("status", "已撤销");
				} else {
					newmap.put("status", "未报到");
				}
			} else {
				newmap.put("status", "");
			}

			// 撤销原因
			if (sim.getCancelReason() != null) {
				newmap.put("cancelReason", sim.getCancelReason());
			} else {
				newmap.put("cancelReason", "");
			}

			// 入学年份
			/*
			 * if(sim.getEnterYear()!=null){ newmap.put("enterYear",
			 * sim.getEnterYear()); }else{ newmap.put("enterYear", ""); }
			 */
			if (sim.getEnterYearDic() != null) {
				newmap.put("enterYear", sim.getEnterYearDic().getCode());
			} else {
				newmap.put("enterYear", "");
			}

			// 报到时间
			if (sim.getReportDate() != null) {
				newmap.put("reportDate",
						AmsDateUtil.getCustomDateString(sim.getReportDate(), "yyyy-MM-dd HH:mm:ss"));
			} else {
				newmap.put("reportDate", "");
			}

			// 报到地点
			if (sim.getReportSiteDic() != null) {
				newmap.put("reportSite", sim.getReportSiteDic().getName());
			} else {
				newmap.put("reportSite", "");
			}

			// 新生采集状态
			if (sim.getCollectState() != null) {
				if ("1".equals(sim.getCollectState())) {
					newmap.put("collectState", "已采集");
				} else {
					newmap.put("collectState", "未采集");
				}
			} else {
				newmap.put("collectState", "未采集");
			}

			// 监护人
			StudentGuardianModel sgm = studentGuardianService
					.getByStudentId(sim.getId());
			if (sgm != null) {
				// 父亲姓名
				if (sgm.getFatherName() != null) {
					newmap.put("fatherName", sgm.getFatherName());
				} else {
					newmap.put("fatherName", "");
				}

				// 父亲手机号码
				if (sgm.getFatherPhone() != null) {
					newmap.put("fatherPhone", sgm.getFatherPhone());
				} else {
					newmap.put("fatherPhone", "");
				}

				// 父亲邮箱
				if (sgm.getFatherEmail() != null) {
					newmap.put("fatherEmail", sgm.getFatherEmail());
				} else {
					newmap.put("fatherEmail", "");
				}

				// 父亲住址
				if (sgm.getFatherAddress() != null) {
					newmap.put("fatherAddress", sgm.getFatherAddress());
				} else {
					newmap.put("fatherAddress", "");
				}

				// 父亲邮编
				if (sgm.getFatherPostCode() != null) {
					newmap.put("fatherPostCode", sgm.getFatherPostCode());
				} else {
					newmap.put("fatherPostCode", "");
				}

				// 父亲工作单位
				if (sgm.getFatherWorkUnit() != null) {
					newmap.put("fatherWorkUnit", sgm.getFatherWorkUnit());
				} else {
					newmap.put("fatherWorkUnit", "");
				}

				// 母亲姓名
				if (sgm.getMotherName() != null) {
					newmap.put("motherName", sgm.getMotherName());
				} else {
					newmap.put("motherName", "");
				}

				// 母亲手机号码
				if (sgm.getMotherPhone() != null) {
					newmap.put("motherPhone", sgm.getMotherPhone());
				} else {
					newmap.put("motherPhone", "");
				}

				// 母亲邮箱
				if (sgm.getMotherEmail() != null) {
					newmap.put("motherEmail", sgm.getMotherEmail());
				} else {
					newmap.put("motherEmail", "");
				}

				// 母亲住址
				if (sgm.getMotherAddress() != null) {
					newmap.put("motherAddress", sgm.getMotherAddress());
				} else {
					newmap.put("motherAddress", "");
				}

				// 母亲邮编
				if (sgm.getMotherPostCode() != null) {
					newmap.put("motherPostCode", sgm.getMotherPostCode());
				} else {
					newmap.put("motherPostCode", "");
				}

				// 母亲工作单位
				if (sgm.getMotherWorkUnit() != null) {
					newmap.put("motherWorkUnit", sgm.getMotherWorkUnit());
				} else {
					newmap.put("motherWorkUnit", "");
				}

				// 监护人姓名
				if (sgm.getGuardianName() != null) {
					newmap.put("guardianName", sgm.getGuardianName());
				} else {
					newmap.put("guardianName", "");
				}

				// 监护人手机号码
				if (sgm.getGuardianPhone() != null) {
					newmap.put("guardianPhone", sgm.getGuardianPhone());
				} else {
					newmap.put("guardianPhone", "");
				}

				// 监护人邮箱
				if (sgm.getGuardianEmail() != null) {
					newmap.put("guardianEmail", sgm.getGuardianEmail());
				} else {
					newmap.put("guardianEmail", "");
				}

				// 监护人住址
				if (sgm.getGuardianAddress() != null) {
					newmap.put("guardianAddress", sgm.getGuardianAddress());
				} else {
					newmap.put("guardianAddress", "");
				}

				// 监护人邮编
				if (sgm.getGuardianPostCode() != null) {
					newmap.put("guardianPostCode", sgm.getGuardianPostCode());
				} else {
					newmap.put("guardianPostCode", "");
				}

				// 监护人工作单位
				if (sgm.getGuardianWorkUnit() != null) {
					newmap.put("guardianWorkUnit", sgm.getGuardianWorkUnit());
				} else {
					newmap.put("guardianWorkUnit", "");
				}

			} else {
				// 父亲
				newmap.put("fatherName", "");
				newmap.put("fatherPhone", "");
				newmap.put("fatherEmail", "");
				newmap.put("fatherAddress", "");
				newmap.put("fatherPostCode", "");
				newmap.put("fatherWorkUnit", "");
				// 母亲
				newmap.put("motherName", "");
				newmap.put("motherPhone", "");
				newmap.put("motherEmail", "");
				newmap.put("motherAddress", "");
				newmap.put("motherPostCode", "");
				newmap.put("motherWorkUnit", "");

				// 监护人
				newmap.put("guardianName", "");
				newmap.put("guardianPhone", "");
				newmap.put("guardianEmail", "");
				newmap.put("guardianAddress", "");
				newmap.put("guardianPostCode", "");
				newmap.put("guardianWorkUnit", "");
			}

			listMap.add(newmap);
		}

		try {
			HSSFWorkbook wb = this.excelService.exportData(
					"export_student_info.xls", "studentInfoExport", listMap);

			// HSSFWorkbook wb=this.excelService.exportData("11.xls", "11",
			// listMap);
			String filename = "学生信息表.xls";
			response.setContentType("application/x-excel");
			response.setHeader("Content-disposition", "attachment;filename="
					+ new String(filename.getBytes("GBK"), "iso-8859-1"));
			response.setCharacterEncoding("UTF-8");
			OutputStream ouputStream = response.getOutputStream();
			wb.write(ouputStream);
			ouputStream.flush();
			ouputStream.close();

		} catch (ExcelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
