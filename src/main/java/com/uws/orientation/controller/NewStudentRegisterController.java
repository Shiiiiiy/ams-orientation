package com.uws.orientation.controller;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uws.common.service.IQuestionNaireService;
import com.uws.common.util.AmsDateUtil;
import com.uws.common.util.QuestionNaireConstants;
import com.uws.common.util.QuestionNaireUtil;
import com.uws.core.base.BaseController;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.CompUtil;
import com.uws.core.util.DataUtil;
import com.uws.core.util.DateUtil;
import com.uws.domain.orientation.StudentGuardianModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.orientation.StudentReportModel;
import com.uws.domain.orientation.WelcomeSetModel;
import com.uws.domain.question.QuestionAnswerBaseModel;
import com.uws.domain.question.QuestionAnswerDetailModel;
import com.uws.domain.question.QuestionInfoItemModel;
import com.uws.domain.question.QuestionInfoModel;
import com.uws.domain.question.QuestionItemOptionModel;
import com.uws.log.Logger;
import com.uws.log.LoggerFactory;
import com.uws.orientation.service.IStudentGuardianService;
import com.uws.orientation.service.IStudentInfoService;
import com.uws.orientation.service.IStudentReportService;
import com.uws.orientation.service.IWelcomeSetService;
import com.uws.sys.model.Dic;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.impl.DicFactory;

/**
 * 
 * @ClassName: NewStudentRegisterController
 * @Description: TODO(新生登录信息采集系统的Controller)
 * @author wangcl
 * @date 2015-7-24 上午9:46:48
 * 
 */
@Controller
public class NewStudentRegisterController extends BaseController {

	// 日志
	private Logger log = new LoggerFactory(NewStudentRegisterController.class);

	// 数据字典处理的Util
	private DicUtil dicUtil = DicFactory.getDicUtil();

	// 学生基本信息的Service
	@Autowired
	private IStudentInfoService studentInfoService;

	// 迎新数据设置的Service
	@Autowired
	private IWelcomeSetService welcomeSetService;

	// 新生监护人的Service
	@Autowired
	private IStudentGuardianService studentGuardianService;

	// 迎新-报到信息的Service
	@Autowired
	private IStudentReportService studentReportService;

	// 问卷填写子模块对外公用接口
	@Autowired
	private IQuestionNaireService questionNaireService;

	// session
	SessionUtil sessionUtil = SessionFactory.getSession("REGISTRER");

	/**
	 * 
	 * @Title: getCurrStudentId
	 * @Description: TODO(把学生Id放入Seesion（会话）中)
	 * @param session
	 * @return void
	 * @author wangcl
	 */
	public static String getCurrStudentId(HttpSession session) {
		// 获取当前登录学生
		StudentInfoModel sessionNewStudent = (StudentInfoModel) session
				.getAttribute("student_key");

		if (DataUtil.isNotNull(sessionNewStudent)) {
			return sessionNewStudent.getId();
		}
		return null;
	}

	/**
	 * 新生信息采集填写说明
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping({ "/newstudent/register/viewWriteComm" })
	public String viewWriteComm(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			Model model) {
		// 菜单标识
		model.addAttribute("menuKey", "register");

		// 迎新数据设置
		WelcomeSetModel wsm = welcomeSetService.getWelcomeSet();

		// 设置的前台页面
		model.addAttribute("welcomeSet", wsm);

		return "orientation/register/viewWriteComm";
	}

	/**
	 * 新生注册初始化编辑页面（基本信息）
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping({ "/newstudent/register/editNewStudentInfo" })
	public String editNewStudentInfo(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			Model model) {

		// 菜单标识
		model.addAttribute("menuKey", "register");

		// 取得当前登录的新生信息
		StudentInfoModel newStudent = studentInfoService
				.getStudentInfoById(getCurrStudentId(session));

		model.addAttribute("newStudent", newStudent);

		// 初始化下拉列表值
		initSelectValue(model);

		// 当前时间是否在设定的范围内
		model.addAttribute("isScope", this.isDateScope());

		// 页面跳转
		return "orientation/register/editNewStudentInfo";
	}

	/**
	 * 新生信息采集--监护人信息
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping({ "/newstudent/register/editGuardian" })
	public String editGuardian(HttpSession session, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		// 菜单标识
		model.addAttribute("menuKey", "register");

		// 监护人信息
		StudentGuardianModel sgm = studentGuardianService
				.getByStudentId(getCurrStudentId(session));

		model.addAttribute("guardian", sgm);

		// 当前时间是否在设定的范围内
		model.addAttribute("isScope", this.isDateScope());

		return "orientation/register/editGuardian";
	}

	/**
	 * 新生信息采集--预报到信息
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping({ "/newstudent/register/editReport" })
	public String editReport(HttpSession session, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		// 菜单标识
		model.addAttribute("menuKey", "register");

		// 报到信息
		StudentReportModel srm = studentReportService
				.getByStudentId(getCurrStudentId(session));
		model.addAttribute("report", srm);

		// 迎新数据设置
		WelcomeSetModel wsm = welcomeSetService.getWelcomeSet();
		model.addAttribute("welcomeSet", wsm);

		// 乘车方式
		List<Dic> rideWayList = dicUtil.getDicInfoList("RIDE_WAY");
		model.addAttribute("rideWayList", rideWayList);

		// 到达的站点
		List<Dic> siteList = dicUtil.getDicInfoList("SITE");
		model.addAttribute("siteList", siteList);

		// 当前时间是否在设定的范围内
		model.addAttribute("isScope", this.isDateScope());

		return "orientation/register/editReport";
	}

	/**
	 * 新生信息采集--回答问卷初始化
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping({ "/newstudent/register/answerQuestion" })
	public String answerQuestion(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			Model model) {
		// 菜单标识
		model.addAttribute("menuKey", "register");

		// 新生的记录号
		String studentId = getCurrStudentId(session);

		// 取得新生问卷
		List<QuestionInfoModel> qifmList = questionNaireService
				.getNewStudentQuestionNaireInfo(studentId);
		// 临时测试用
		// List<QuestionInfoModel> qifmList =
		// questionNaireService.getQuestionNaireInfoList();
		// 问卷
		QuestionInfoModel qim = null;
		// 单选题
		List<QuestionInfoItemModel> singleItemList = null;
		// 多选题
		List<QuestionInfoItemModel> mulItemList = null;
		// 问答题
		List<QuestionInfoItemModel> subItemList = null;
		// 单选题选项
		List<QuestionItemOptionModel> singleQuesOptionList = null;
		// 多选题选项
		List<QuestionItemOptionModel> mulQuesOptionList = null;
		// 问答题答题列表
		List<QuestionItemOptionModel> subQuesAnswerList = null;
		if (qifmList != null && qifmList.size() > 0) {
			qim = qifmList.get(0);
			// 获取问卷主键
			String questionNaireId = qim.getId();
			// 获取问卷单选题列表
			singleItemList = this.questionNaireService
					.getQuestionNaireSingleItemList(questionNaireId);
			// 获取问卷多选题列表
			mulItemList = this.questionNaireService
					.getQuestionNaireMulItemList(questionNaireId);
			// 获取问卷问答题列表
			subItemList = this.questionNaireService
					.getQuestionNaireSubItemList(questionNaireId);
			// 获取单选题答题列表
			singleQuesOptionList = this.questionNaireService
					.getSingleQuestionOption(studentId, questionNaireId);
			// 获取多选题答题列表
			mulQuesOptionList = this.questionNaireService.getMulQuestionOption(
					studentId, questionNaireId);
			// 获取问答题答题列表
			subQuesAnswerList = this.questionNaireService
					.getAnswerQuestionOption(studentId, questionNaireId);
		} else {
			// 没有回答问卷的情况的处理

		}
		model.addAttribute("qim", qim);
		model.addAttribute("singleItemList", singleItemList);
		model.addAttribute("mulItemList", mulItemList);
		model.addAttribute("subItemList", subItemList);
		model.addAttribute("singleQuesOptionList", singleQuesOptionList);
		model.addAttribute("mulQuesOptionList", mulQuesOptionList);
		model.addAttribute("subQuesAnswerList", subQuesAnswerList);
		model.addAttribute("splitFlag",
				QuestionNaireConstants.AMS_SPLIT_FLAG_QUESTIONNAIRE);

		if (qim != null) {
			// 取得状态
			boolean flag = this.questionNaireService.isSubmitQuestionNaire(
					studentId, qim.getId());
			if (flag) {
				model.addAttribute("status", "submit");
			} else {
				model.addAttribute("status", "save");
			}
		} else {
			model.addAttribute("status", "null");
			model.addAttribute("qim", new QuestionInfoModel());

		}

		// 当前时间是否在设定的范围内
		model.addAttribute("isScope", this.isDateScope());

		return "orientation/register/answerQuestion";
	}

	/**
	 * 保存当前问卷
	 * 
	 * @param request
	 *            当次请求
	 * @param response
	 *            当次响应
	 * @param model
	 *            页面模型
	 * @param questionNairePo
	 *            问卷对象
	 * @param singleOption
	 *            单选选中选项
	 * @param mulOption
	 *            多选选中选项
	 * @param singleQadms
	 *            单选题答案数组
	 * @param mulQadms
	 *            多选题答案数组
	 * @param answerQadms
	 *            问答题答案数组
	 * @return 答题列表
	 */
	@RequestMapping({ "/newstudent/register/saveQuestion" })
	public String saveCurPaper(HttpSession session, HttpServletRequest request,
			HttpServletResponse response, ModelMap model,
			QuestionInfoModel questionNairePo, String[] singleOption,
			String[] mulOption, String[] singleQadms, String[] mulQadms,
			String[] answerQadms, String[] subItemAreas) {
		// 新生的记录号
		String id = getCurrStudentId(session);
		// 提交类型
		String type = request.getParameter("type");

		// 设置Id
		questionNairePo.setId(questionNairePo.getQuestionNaireId());

		if ("upGo".equals(type)) {
			// 直接跳转到上一页
			return "redirect:/newstudent/register/editReport.do";
		}

		// 答卷基本信息
		QuestionAnswerBaseModel qabm = this.getQuestionNaireBaseInfo(
				questionNairePo, id);
		// 答卷单选题信息
		List<QuestionAnswerDetailModel> singleQadmList = QuestionNaireUtil
				.getSingleQuestionNaireDetailInfo(singleOption, singleQadms,
						qabm, id);
		// 答卷多选题信息
		List<QuestionAnswerDetailModel> mulQadmList = QuestionNaireUtil
				.getMulQuestionNaireDetailInfo(mulOption, mulQadms, qabm, id);
		// 答卷问答题信息
		List<QuestionAnswerDetailModel> answerQadmList = QuestionNaireUtil
				.getAnswerQuestionNaireDetailInfo(answerQadms, qabm,
						subItemAreas, id);

		// 根据提交的类型判断页面的跳转（下一页、上一页、保存）
		if ("save".equals(type)) {
			// 保存
			// 保存当前问卷信息
			this.questionNaireService.saveCurQuestionNaire(qabm,
					singleQadmList, mulQadmList, answerQadmList);

			return "redirect:/newstudent/register/answerQuestion.do";
			// 本页
			// return "redirect:/newstudent/register/editReport.do";
		} else if ("up".equals(type)) {
			// 上一页
			// 保存当前问卷信息
			this.questionNaireService.saveCurQuestionNaire(qabm,
					singleQadmList, mulQadmList, answerQadmList);
			return "redirect:/newstudent/register/editReport.do";
		} else if ("upGoSumbit".equals(type)) {
			//没有问卷的情况下进行提交
			// 修改学生基本信息、监护人信息、预报到信息的提交状态
			updateStudentInfoSubmitStatus(id);
			// 上一页
			//return "redirect:/newstudent/register/editReport.do";
			//返回到信息填写说明页面
			return "redirect:/newstudent/register/viewWriteComm.do";
		} else {

			// 提交当前问卷信息
			this.questionNaireService.submitCurQuestionNaire(qabm,
					singleQadmList, mulQadmList, answerQadmList);
			// 修改学生基本信息、监护人信息、预报到信息的提交状态
			updateStudentInfoSubmitStatus(id);
			// 本页
			return "redirect:/newstudent/register/answerQuestion.do";

		}

	}

	/**
	 * 新生信息采集--基本信息保存
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = { "/newstudent/register/saveNewStudentInfo" }, method = RequestMethod.POST)
	public String saveNewStudentInfo(HttpServletRequest request,
			HttpServletResponse response, Model model,
			StudentInfoModel newStudent) {
		// 新生的记录号
		String id = request.getParameter("id");
		// 提交类型
		String type = request.getParameter("type");

		if (DataUtil.isNotNull(id)) {
			// 进行更新处理
			studentInfoService.updateStudentInfo(newStudent);
		} else {
			// 本页
			return "redirect:/newstudent/register/editNewStudentInfo.do";
		}
		// 根据提交的类型判断页面的跳转（下一页、上一页、保存）
		if ("next".equals(type)) {
			// 下一页
			return "redirect:/newstudent/register/editGuardian.do";
		} else if ("up".equals(type)) {
			// 上一页
			return "redirect:/newstudent/register/viewWriteComm.do";
		} else {
			// 本页
			return "redirect:/newstudent/register/editNewStudentInfo.do";
		}

	}

	/**
	 * 新生信息采集--监护人信息保存
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = { "/newstudent/register/saveGuardian" }, method = RequestMethod.POST)
	public String saveNewStudentGuardian(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			Model model, StudentGuardianModel guardian) {
		// 新生信息
		StudentInfoModel newStudent = studentInfoService
				.getStudentInfoById(getCurrStudentId(session));
		// 父亲的记录号
		String fatherId = request.getParameter("fatherId");
		// 对父亲进行处理
		StudentGuardianModel fatherGuardian = new StudentGuardianModel();
		// 新生
		fatherGuardian.setStudentInfo(newStudent);
		// 父亲姓名
		fatherGuardian.setGuardianName(guardian.getFatherName());
		// 父亲手机号码
		fatherGuardian.setGuardianPhone(guardian.getFatherPhone());
		// 父亲住址
		fatherGuardian.setGuardianAddress(guardian.getFatherAddress());
		// 父亲姓名
		fatherGuardian.setGuardianEmail(guardian.getFatherEmail());
		// 父亲手机号码
		fatherGuardian.setGuardianPostCode(guardian.getFatherPostCode());
		// 父亲工作单位
		fatherGuardian.setGuardianWorkUnit(guardian.getFatherWorkUnit());

		// 顺序号
		fatherGuardian.setSeqNum("1");
		if (DataUtil.isNotNull(fatherId)) {
			// 父亲Id
			fatherGuardian.setId(fatherId);
			// 进行更新处理
			studentGuardianService.updateStudentGuardian(fatherGuardian);
		} else {
			// 父亲的状态是保存
			// fatherGuardian.setStatus("0");
			// 进行保存处理
			studentGuardianService.saveStudentGuardian(fatherGuardian);
		}
		// 母亲的记录号
		String motherId = request.getParameter("motherId");
		StudentGuardianModel motherGuardian = new StudentGuardianModel();
		// 新生
		motherGuardian.setStudentInfo(newStudent);
		// 母亲姓名
		motherGuardian.setGuardianName(guardian.getMotherName());
		// 母亲手机号码
		motherGuardian.setGuardianPhone(guardian.getMotherPhone());
		// 母亲住址
		motherGuardian.setGuardianAddress(guardian.getMotherAddress());
		// 母亲姓名
		motherGuardian.setGuardianEmail(guardian.getMotherEmail());
		// 母亲手机号码
		motherGuardian.setGuardianPostCode(guardian.getMotherPostCode());
		// 母亲工作单位
		motherGuardian.setGuardianWorkUnit(guardian.getMotherWorkUnit());

		// 顺序号
		motherGuardian.setSeqNum("2");

		if (DataUtil.isNotNull(motherId)) {
			// 母亲Id
			motherGuardian.setId(motherId);
			// 进行更新处理
			studentGuardianService.updateStudentGuardian(motherGuardian);
		} else {
			// 母亲的状态是保存
			// motherGuardian.setStatus("0");
			// 进行保存处理
			studentGuardianService.saveStudentGuardian(motherGuardian);
		}

		// 监护人的记录号
		String guardianId = request.getParameter("guardianId");
		// 新生
		guardian.setStudentInfo(newStudent);

		// 顺序号
		guardian.setSeqNum("3");

		if (DataUtil.isNotNull(guardianId)) {
			// 监护人Id
			guardian.setId(guardianId);
			// 进行更新处理
			studentGuardianService.updateStudentGuardian(guardian);
		} else {
			// 监护人的状态是保存
			// guardian.setStatus("0");
			// 进行保存处理
			studentGuardianService.saveStudentGuardian(guardian);
		}

		// 提交类型
		String type = request.getParameter("type");

		// 根据提交的类型判断页面的跳转（下一页、上一页、保存）
		if ("next".equals(type)) {
			// 下一页
			return "redirect:/newstudent/register/editReport.do";
		} else if ("up".equals(type)) {
			// 上一页
			return "redirect:/newstudent/register/editNewStudentInfo.do";
		} else {
			// 本页
			return "redirect:/newstudent/register/editGuardian.do";
		}

	}

	/**
	 * 新生信息采集--报到信息保存
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = { "/newstudent/register/saveNewReport" }, method = RequestMethod.POST)
	public String saveNewStudentReport(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			Model model, StudentReportModel newReport) {
		// 新生信息
		StudentInfoModel newStudent = studentInfoService
				.getStudentInfoById(getCurrStudentId(session));

		// 报到信息的ID
		String id = request.getParameter("id");

		// 新生
		newReport.setStudentInfo(newStudent);

		// 提交类型
		String type = request.getParameter("type");

		if (DataUtil.isNotNull(id)) {
			// 进行更新处理
			studentReportService.updateStudentReport(newReport);
		} else {
			// 进行保存处理
			studentReportService.saveStudentReport(newReport);
			// 本页
			return "redirect:/newstudent/register/editReport.do";
		}

		// 根据提交的类型判断页面的跳转（下一页、上一页、保存）
		if ("next".equals(type)) {
			// 下一页
			return "redirect:/newstudent/register/answerQuestion.do";
			// 本页
			// return "redirect:/newstudent/register/editReport.do";
		} else if ("up".equals(type)) {
			// 上一页
			return "redirect:/newstudent/register/editGuardian.do";
		} else {
			// 本页
			return "redirect:/newstudent/register/editReport.do";
		}

	}

	/**
	 * 
	 * @Title: editPassword
	 * @Description: TODO(跳转到密码修改页面的处理)
	 * @param response
	 * @param request
	 * @param model
	 * @return String
	 * @author wangcl
	 */
	@RequestMapping({ "/newstudent/register/editPassWord" })
	public String editPassword(HttpSession session, HttpServletRequest request,
			HttpServletResponse response, Model model) {

		// 菜单标识
		model.addAttribute("menuKey", "editPassword");

		return "orientation/register/editPassword";
	}

	/**
	 * 
	 * @Title: saveNewPassword
	 * @Description: TODO(密码修改页面的处理)
	 * @param oldPassWord
	 *            旧密码
	 * @param newPassWord
	 *            新密码
	 * @param session
	 *            session
	 * @param verifyPassWord
	 *            确认密码
	 * @return String
	 * @author wangcl
	 */
	@RequestMapping(value = { "/newstudent/register/saveNewPassword" }, produces = { "text/plain;charset=UTF-8" })
	@ResponseBody
	public String saveNewPassword(@RequestParam String oldPassWord,
			@RequestParam String newPassWord, HttpSession session,
			@RequestParam String verifyPassWord) {
		Map<String, String> map = new HashMap<String, String>();
		if ((DataUtil.isNull(oldPassWord)) || (DataUtil.isNull(newPassWord))
				|| (DataUtil.isNull(verifyPassWord))) {
			map.put("result", "error");
			map.put("message", "密码不能为空");
			return CompUtil.mapToJson(map);
		}
		if (!newPassWord.equals(verifyPassWord)) {
			map.put("result", "error");
			map.put("message", "新密码与确认密码不一致");
			return CompUtil.mapToJson(map);
		}
		try {
			if (!studentInfoService.queryCheckNewStudentPassword(
					getCurrStudentId(session), oldPassWord)) {
				map.put("result", "error_mmbzq");
				map.put("message", "用户密码不正确");
				return CompUtil.mapToJson(map);
			}
		} catch (NoSuchAlgorithmException e) {
			log.error(newPassWord + "通过MD5加密失败:" + e.getMessage());
			map.put("result", "error");
			map.put("message", "用户密码错误");
			return CompUtil.mapToJson(map);
		}
		try {
			studentInfoService.updateNewStudentPassWord(
					getCurrStudentId(session), newPassWord);
			map.put("result", "success");
			map.put("message", "修改用户密码成功");
			return CompUtil.mapToJson(map);
		} catch (NoSuchAlgorithmException e) {
			log.error(newPassWord + "通过MD5加密失败:" + e.getMessage());
			map.put("result", "error");
			map.put("message", "修改用户密码失败");
			return CompUtil.mapToJson(map);
		}
	}

	/**
	 * 
	 * @Title: initSelectValue
	 * @Description: TODO(页面初始化时的信息)
	 * @param model
	 * @return void
	 * @author wangcl
	 */
	public void initSelectValue(Model model) {

		// 初始化下拉列表 性别项
		List<Dic> genderList = dicUtil.getDicInfoList("GENDER");
		model.addAttribute("genderList", genderList);

		// 初始化下拉列表 政治面貌项
		List<Dic> politicalList = dicUtil
				.getDicInfoList("SCH_POLITICAL_STATUS");
		model.addAttribute("politicalList", politicalList);

		// 初始化下拉列表 民族项
		List<Dic> nationalList = dicUtil.getDicInfoList("NATION");
		model.addAttribute("nationalList", nationalList);

		// 初始化下拉列表 信仰项
		List<Dic> creedList = dicUtil.getDicInfoList("CREED");
		model.addAttribute("creedList", creedList);

		// 初始化下拉列表 户口类别项
		List<Dic> accountTypeList = dicUtil.getDicInfoList("ACCOUNT_TYPE");
		model.addAttribute("accountTypeList", accountTypeList);

		// 初始化下拉列表 血型项
		List<Dic> bloodTypeList = dicUtil.getDicInfoList("BLOOD_TYPE");
		model.addAttribute("bloodTypeList", bloodTypeList);

		// 初始化下拉列表 籍贯项
		List<Dic> nativeList = dicUtil.getDicInfoList("NATIVE");
		model.addAttribute("nativeList", nativeList);

		// 初始化下拉列表 户口类别
		List<Dic> accountList = dicUtil.getDicInfoList("ACCOUNT_TYPE");
		model.addAttribute("accountList", accountList);

		// 初始化下拉列表 婚姻状况
		List<Dic> marriageList = dicUtil.getDicInfoList("MARRIAGE");
		model.addAttribute("marriageList", marriageList);

		// 初始化下拉列表 港澳台侨外状况
		List<Dic> overChineseList = dicUtil.getDicInfoList("OVER_CHINESE");
		model.addAttribute("overChineseList", overChineseList);

		// 初始化下拉列表 健康状况
		List<Dic> healthStateList = dicUtil.getDicInfoList("HEALTH_STATE");
		model.addAttribute("healthSateList", healthStateList);

	}

	/**
	 * 获取答卷的基本信息
	 * 
	 * @param questionNairePo
	 *            问卷对象
	 * @return 答卷基本信息
	 */
	private QuestionAnswerBaseModel getQuestionNaireBaseInfo(
			QuestionInfoModel questionNairePo, String studentId) {

		QuestionAnswerBaseModel qabm = this.questionNaireService
				.getQuesNaireBaseModel(studentId,
						questionNairePo.getQuestionNaireId());
		if (!DataUtil.isNotNull(qabm)) {
			qabm = QuestionNaireUtil.formateQuestionNaireBaseInfo(
					questionNairePo, studentId);
		}
		return qabm;
	}

	/**
	 * 修改学生基本信息、监护人信息、预报到信息的状态
	 * 
	 * @param 学生Id
	 * @return 答案详细信息
	 */

	private void updateStudentInfoSubmitStatus(String studentId) {
		// 基本信息
		StudentInfoModel sifm = studentInfoService
				.getStudentInfoById(studentId);
		if (sifm != null) {
			// 提交状态
			sifm.setCollectState("1");
			studentInfoService.updateStudentInfo(sifm);
		}
		// 监护人信息(父亲)
		StudentGuardianModel sgmF = studentGuardianService
				.getByStudentIdAndSeqNum(studentId, "1");
		if (sgmF != null) {
			sgmF.setStatus("1");
			studentGuardianService.updateStudentGuardian(sgmF);
		}
		// 监护人信息(父亲)
		StudentGuardianModel sgmM = studentGuardianService
				.getByStudentIdAndSeqNum(studentId, "2");
		if (sgmF != null) {
			sgmM.setStatus("1");
			studentGuardianService.updateStudentGuardian(sgmM);
		}

		// 监护人信息(父亲)
		StudentGuardianModel sgmG = studentGuardianService
				.getByStudentIdAndSeqNum(studentId, "3");
		if (sgmF != null) {
			sgmG.setStatus("1");
			studentGuardianService.updateStudentGuardian(sgmG);
		}

		// 预报到信息
		StudentReportModel srm = studentReportService.getByStudentId(studentId);
		if (srm != null) {
			// 提交状态
			srm.setStatus("1");
			studentReportService.updateStudentReport(srm);
		}

	}

	/**
	 * 
	 * @Title: isDateSocpe
	 * @Description: TODO(把当前时间是否在设定的范围内1:表示在范围内，0:表示不 在范围内)
	 * @return void
	 * @author wangcl
	 */
	private String isDateScope() {
		// 当前时间的范围
		String isSocpe = "1";
		// 迎新数据设置
		WelcomeSetModel wsm = welcomeSetService.getWelcomeSet();

		if (wsm.getStartDate() != null && wsm.getEndDate() != null) {

			// 判断
			if (!AmsDateUtil.isDateScope(new Date(), wsm.getStartDate(),
					wsm.getEndDate())) {
				isSocpe = "当前登录的时间不在学校开放的时间（"
						+ DateUtil.getCustomDateString(wsm.getStartDate(),
								"yyyy-MM-dd")
						+ "--"
						+ DateUtil.getCustomDateString(wsm.getEndDate(),
								"yyyy-MM-dd") + "）内，您只能查看自己填写的信息！";
			}
		} else {
			isSocpe = "系统还没有设置开放时间，您只能查看信息不能填写，请跟学校招生就业处老师联系！";
		}

		return isSocpe;
	}
}
