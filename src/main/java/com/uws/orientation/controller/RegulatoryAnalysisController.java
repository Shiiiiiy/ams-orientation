package com.uws.orientation.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.hibernate.mapping.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uws.common.service.IBaseDataService;
import com.uws.common.util.AmsDateUtil;
import com.uws.common.util.SchoolYearUtil;
import com.uws.comp.service.ICompService;
import com.uws.core.base.BaseController;
import com.uws.core.excel.ExcelException;
import com.uws.core.excel.service.IExcelService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.util.DataUtil;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseMajorModel;
import com.uws.domain.orientation.CollegeRegistedVo;
import com.uws.domain.orientation.CountExReportVo;
import com.uws.domain.orientation.PaidVo;
import com.uws.domain.orientation.PlaceCountVo;
import com.uws.domain.orientation.ReportProgressCountVo;
import com.uws.domain.orientation.ReportProgressVo;
import com.uws.domain.orientation.ReportStatisticsVo;
import com.uws.domain.orientation.ReportVo;
import com.uws.domain.orientation.StudentGuardianModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.orientation.StudentReportModel;
import com.uws.domain.orientation.TimeCountVo;
import com.uws.log.LoggerFactory;
import com.uws.orientation.service.IRegulatoryAnalysisService;
import com.uws.orientation.service.IStudentGuardianService;
import com.uws.orientation.util.Constants;
import com.uws.sys.model.Dic;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.IDicService;
import com.uws.sys.service.impl.DicFactory;
import com.uws.util.CheckUtils;
import com.uws.util.ProjectSessionUtils;

@Controller
public class RegulatoryAnalysisController extends BaseController {

	// 日志
	private LoggerFactory log = new LoggerFactory(
			RegulatoryAnalysisController.class);
	// 数据字典工具类
	private DicUtil dicUtil = DicFactory.getDicUtil();

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				dateFormat, true));
	}

	@Autowired
	IRegulatoryAnalysisService regulatoryAnalysisService;

	@Autowired
	private IExcelService excelService;

	@Autowired
	private IBaseDataService baseDataService;

	@Autowired
	private ICompService compService;

	@Autowired
	private IDicService dicService;

	@Autowired
	private IStudentGuardianService studentGuardianService;

	/**
	 * 预期报到查询页面
	 * 
	 * @param model
	 * @param request
	 * @param studentReportModel
	 * @return
	 */
	@RequestMapping(value = { Constants.REGULATORY_ANALYSIS
			+ "/opt-query/queryExReport" })
	public String listExReportList(ModelMap model, HttpServletRequest request,
			StudentReportModel studentReportModel) {
		log.info("预期报到查询页面");
		String collegeId = ProjectSessionUtils.getCurrentTeacherOrgId(request);
		if (CheckUtils.isCurrentOrgEqCollege(collegeId)) {
			BaseAcademyModel ba = new BaseAcademyModel();
			ba.setId(collegeId);
			StudentInfoModel sim = studentReportModel.getStudentInfo();
			if (sim == null)
				sim = new StudentInfoModel();
			sim.setCollege(ba);
			studentReportModel.setStudentInfo(sim);
			model.addAttribute("flag", true);
		}
		// 如果为空，默认当前学年
		if (studentReportModel == null
				|| studentReportModel.getYearDic() == null
				|| studentReportModel.getYearDic().getId() == null)
			studentReportModel.setYearDic(currentYearDic());
		Integer pageNo = request.getParameter("pageNo") != null ? Integer
				.valueOf(request.getParameter("pageNo")) : 1;
		Page page = regulatoryAnalysisService.queryPageExReport(
				Page.DEFAULT_PAGE_SIZE, pageNo, studentReportModel);
		// 学院
		List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		List<BaseMajorModel> majorList = null;
		if (null != studentReportModel
				&& studentReportModel.getStudentInfo() != null
				&& null != studentReportModel.getStudentInfo().getCollege()
				&& null != studentReportModel.getStudentInfo().getCollege()
						.getId()
				&& !studentReportModel.getStudentInfo().getCollege().getId()
						.equals("")) {
			majorList = compService.queryMajorByCollage(studentReportModel
					.getStudentInfo().getCollege().getId());
			log.debug("若已经选择学院，则查询学院下的专业信息.");
		}
		List<BaseClassModel> classList = null;
		if (null != studentReportModel
				&& studentReportModel.getStudentInfo() != null
				&& studentReportModel.getStudentInfo().getMajor() != null) {
			classList = compService.queryClassByMajor(studentReportModel
					.getStudentInfo().getMajor().getId());
		}
		// 学年
		model.addAttribute("yearList", dicUtil.getDicInfoList("YEAR"));
		// 学院
		model.addAttribute("collegeList", collegeList);
		// 专业
		model.addAttribute("majorList", majorList);
		// 班级
		model.addAttribute("classList", classList);
		// 乘车方式
		model.addAttribute("rideWayList", dicUtil.getDicInfoList("RIDE_WAY"));
		// 到达的站点
		model.addAttribute("siteList", dicUtil.getDicInfoList("SITE"));
		// 是否（需不需要）列表
		List<String> yOrNList = new ArrayList<String>();

		yOrNList.add(Constants.REGULATORY_NEED);
		yOrNList.add(Constants.REGULATORY_NEED_NOT);

		model.addAttribute("yOrNList", yOrNList);
		model.addAttribute("studentReportModel", studentReportModel);
		model.addAttribute("page", page);

		return Constants.REGULATORY_ANALYSIS + "/exReportList";
	}

	/**
	 * 进入预报道查询导出页面
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = Constants.REGULATORY_ANALYSIS
			+ "/nsm/exportReportListView")
	public String exportReportListView(ModelMap model,
			HttpServletRequest request) {

		log.info("进入预报到查询导出页面");
		String size = request.getParameter("exportSize");
		String page = request.getParameter("pageTotalCount");
		int exportSize = Integer.valueOf(
				(size != null && !size.equals("")) ? size : "1").intValue();
		int pageTotalCount = Integer.valueOf(
				(page != null && !page.equals("")) ? page : "1").intValue();
		int maxNumber = 0;
		if (pageTotalCount < exportSize) {
			maxNumber = 1;
		} else if (pageTotalCount % exportSize == 0) {
			maxNumber = pageTotalCount / exportSize;
		} else {
			maxNumber = pageTotalCount / exportSize + 1;
		}
		model.addAttribute("exportSize", Integer.valueOf(exportSize));
		model.addAttribute("maxNumber", Integer.valueOf(maxNumber));
		// 为了能将导出的数据效率高，判断每次导出数据500条
		if (maxNumber < 500) {
			model.addAttribute("isMore", "false");
		} else {
			model.addAttribute("isMore", "true");
		}
		return Constants.REGULATORY_ANALYSIS + "/exportReportListView";
	}

	/**
	 * 导出预期报到查询
	 * 
	 * @param model
	 * @param request
	 * @param demoPo
	 * @param response
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = Constants.REGULATORY_ANALYSIS
			+ "/opt-export/exportCountReportList")
	public void exportCountReportList(ModelMap model,
			HttpServletRequest request, HttpServletResponse response,
			StudentReportModel studentReportModel) throws ParseException {
		log.info("导出预期报到查询方法");

		String exportSize = request.getParameter("countReportList_exportSize");
		String exportPage = request.getParameter("countReportList_exportPage");

		String collegeId = ProjectSessionUtils.getCurrentTeacherOrgId(request);
		if (CheckUtils.isCurrentOrgEqCollege(collegeId)) {
			BaseAcademyModel ba = new BaseAcademyModel();
			ba.setId(collegeId);
			StudentInfoModel sim = studentReportModel.getStudentInfo();
			if (sim == null)
				sim = new StudentInfoModel();
			sim.setCollege(ba);
			studentReportModel.setStudentInfo(sim);
			model.addAttribute("flag", true);
		}
		List<Map> listMap = new ArrayList<Map>();
		Page listCountExReportListPage = regulatoryAnalysisService
				.countExReportList(exportSize, exportPage, studentReportModel);

		@SuppressWarnings("unchecked")
		List<StudentReportModel> studentList = (List<StudentReportModel>) listCountExReportListPage
				.getResult();
		// System.out.println("demoList.size():"+demoList.size());
		// List<StudentInfoModel> studentList =
		// studentInfoService.getStudentInfoByReport(student);

		for (int i = 0; i < studentList.size(); i++) {
			Map<String, Object> newmap = new HashMap<String, Object>();
			// 序号
			newmap.put("indexs", String.valueOf(i + 1));
			studentReportModel = studentList.get(i);
			// 学生信息
			StudentInfoModel sim = studentReportModel.getStudentInfo();

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
				newmap.put("birthDate", AmsDateUtil.getCustomDateString(
						sim.getBrithDate(), "yyyy-MM-dd"));
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
			/*
			 * if (sim.getSourceLand() != null) { newmap.put("sorceLand",
			 * sim.getSourceLand()); } else { newmap.put("sorceLand", ""); }
			 */
			// 生源地

			newmap.put("sorceLand", null == sim.getCandidateProvence() ? ""
					: sim.getCandidateProvence().getName());

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
				newmap.put("reportDate", AmsDateUtil.getCustomDateString(
						sim.getReportDate(), "yyyy-MM-dd HH:mm:ss"));
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

			newmap.put(
					"testNo",
					StringUtils.isEmpty(sim.getCandidateNum()) ? "" : sim
							.getCandidateNum());
			newmap.put("yearDic", null == studentReportModel.getYearDic() ? ""
					: studentReportModel.getYearDic().getName());
			newmap.put("reason", StringUtils.isEmpty(studentReportModel
					.getReason()) ? "" : studentReportModel.getReason());
			newmap.put(
					"reportDateStr",
					null == studentReportModel.getReportDate() ? ""
							: AmsDateUtil.getCustomDateString(
									studentReportModel.getReportDate(),
									"yyyy-MM-dd HH:mm:ss"));

			newmap.put(
					"trainNumber",
					StringUtils.isEmpty(studentReportModel.getTrainNumber()) ? ""
							: studentReportModel.getTrainNumber());
			newmap.put("siteDic", null == studentReportModel.getSiteDic() ? ""
					: studentReportModel.getSiteDic().getName());
			newmap.put("rideWayDic",
					null == studentReportModel.getRideWayDic() ? ""
							: studentReportModel.getRideWayDic().getName());
			newmap.put("together",
					null == studentReportModel.getTogether() ? ""
							: studentReportModel.getTogether() + "");
			newmap.put("trainNumber", StringUtils.isEmpty(studentReportModel
					.getIsCar()) ? "" : studentReportModel.getIsCar());

			newmap.put("airCond", StringUtils.isEmpty(studentReportModel
					.getAirCond()) ? "" : (studentReportModel.getAirCond()
					.equals("1") ? "需要" : "不需要"));

			newmap.put("bed",
					StringUtils.isEmpty(studentReportModel.getBed()) ? ""
							: (studentReportModel.getBed().equals("1") ? "需要"
									: "不需要"));
			newmap.put("move",
					StringUtils.isEmpty(studentReportModel.getMove()) ? ""
							: (studentReportModel.getMove().equals("1") ? "需要"
									: "不需要"));

			listMap.add(newmap);
		}

		try {
			HSSFWorkbook wb = this.excelService.exportData(
					"export_exportCountExReportListVo.xls",
					"exportCountExReportListVo", listMap);

			// HSSFWorkbook wb=this.excelService.exportData("11.xls", "11",
			// listMap);
			String filename = "预报道学生信息表.xls";
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

	/**
	 * 预期报到统计方法
	 * 
	 * @param model
	 * @param request
	 * @param startDate
	 *            开始时间 年月日时分
	 * @param endDate
	 *            结束时间 年月日时分
	 * @param apartMinute
	 *            间断时间 分钟
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = { Constants.EX_REPORT_COUNT
			+ "/opt-query/countExReport" })
	public String countExReport(ModelMap model, HttpServletRequest request,
			String startDate, String endDate, Integer apartMinute)
			throws ParseException {
		log.info("预期报到统计方法");
		// 添加学院条件
		String collegeId = ProjectSessionUtils.getCurrentTeacherOrgId(request);
		if (!CheckUtils.isCurrentOrgEqCollege(collegeId)) {
			collegeId = null;
		}
		List<CountExReportVo> list = regulatoryAnalysisService.countExReport(
				startDate, endDate, apartMinute, collegeId);
		String[] sd = null;
		List<StudentReportModel> listTotal = new ArrayList<StudentReportModel>();
		Integer total = 0;
		if (startDate != null && !startDate.equals("")) {
			// 年、月、日时分秒字符串的数组，为了取年的字符串
			sd = startDate.split("-");
			listTotal = regulatoryAnalysisService
					.getAllStudentReportModel(dicUtil.getDicInfo("YEAR", sd[0]));
			total = (listTotal != null) ? listTotal.size() : 0;
		}

		model.addAttribute("TOTAL", total);
		model.addAttribute("list", list);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("apartMinute", apartMinute);
		model.addAttribute("pageTotalCount", (list != null) ? list.size() : 0);
		return Constants.REGULATORY_ANALYSIS + "/countExReport";
	}

	/**
	 * 进入预期报到导出页面
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = Constants.EX_REPORT_COUNT
			+ "/nsm/exportCountExReportView")
	public String exportCountExReportView(ModelMap model,
			HttpServletRequest request) {

		log.info("进入导出页面");
		String size = request.getParameter("exportSize");
		String page = request.getParameter("pageTotalCount");
		int exportSize = Integer.valueOf(
				(size != null && !size.equals("")) ? size : "1").intValue();
		int pageTotalCount = Integer.valueOf(
				(page != null && !page.equals("")) ? page : "1").intValue();
		int maxNumber = 0;
		if (pageTotalCount < exportSize) {
			maxNumber = 1;
		} else if (pageTotalCount % exportSize == 0) {
			maxNumber = pageTotalCount / exportSize;
		} else {
			maxNumber = pageTotalCount / exportSize + 1;
		}
		model.addAttribute("exportSize", Integer.valueOf(exportSize));
		model.addAttribute("maxNumber", Integer.valueOf(maxNumber));
		// 为了能将导出的数据效率高，判断每次导出数据500条
		if (maxNumber < 500) {
			model.addAttribute("isMore", "false");
		} else {
			model.addAttribute("isMore", "true");
		}
		return Constants.REGULATORY_ANALYSIS + "/exportCountExReportView";
	}

	/**
	 * 导出预期报到
	 * 
	 * @param model
	 * @param request
	 * @param demoPo
	 * @param response
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = Constants.EX_REPORT_COUNT
			+ "/opt-export/exportCountExReport")
	public void exportCountExReport(ModelMap model, HttpServletRequest request,
			HttpServletResponse response, String startDate, String endDate,
			Integer apartMinute) throws ParseException {
		log.info("导出预期报到统计表方法");
		// 添加学院条件
		String collegeId = ProjectSessionUtils.getCurrentTeacherOrgId(request);
		if (!CheckUtils.isCurrentOrgEqCollege(collegeId)) {
			collegeId = null;
		}
		String exportSize = request
				.getParameter("countExReportQuery_exportSize");
		String exportPage = request
				.getParameter("countExReportQuery_exportPage");
		List<Map> listMap = new ArrayList<Map>();
		List<CountExReportVo> listCountExReportVo = regulatoryAnalysisService
				.countExReport(startDate, endDate, apartMinute, collegeId);
		String[] sd = null;
		List<StudentReportModel> listTotal = new ArrayList<StudentReportModel>();
		Integer total = 0;
		if (startDate != null && !startDate.equals("")) {
			sd = startDate.split("-");
			listTotal = regulatoryAnalysisService
					.getAllStudentReportModel(dicUtil.getDicInfo("YEAR", sd[0]));
			total = (listTotal != null) ? listTotal.size() : 0;
		}
		// 遍历要导出的数据，并将数据放入map对象中
		for (CountExReportVo c : listCountExReportVo) {
			Map<String, Object> newmap = new HashMap<String, Object>();
			newmap.put("timeLine", (c.getName() != null) ? c.getName() : "");
			Integer freshMan = (c.getNum() != null) ? c.getNum() : 0;
			Integer car = (c.getCars() != null) ? c.getCars() : 0;
			Integer bed = (c.getBeds() != null) ? c.getBeds() : 0;
			Integer move = (c.getMoves() != null) ? c.getMoves() : 0;
			if (total != 0) {
				newmap.put("freshMan", freshMan + " / " + (freshMan / total));
				newmap.put("bed", bed + " / " + (bed / total));
				newmap.put("move", move + " / " + (move / total));
			} else {
				newmap.put("freshMan", "0 / 0");
				newmap.put("bed", "0 / 0");
				newmap.put("move", "0 / 0");
			}
			newmap.put("entourage",
					(c.getTogethers() != null) ? c.getTogethers() : "");
			listMap.add(newmap);
		}
		try {
			HSSFWorkbook wb = excelService.exportData(
					"export_exportCountExReportVo.xls",
					"exportCountExReportVo", listMap);
			String filename = (startDate + "至" + endDate) + "预期报到统计表"
					+ (exportPage != null ? exportPage : "") + ".xls";
			response.setContentType("application/x-excel");
			response.setHeader("Content-disposition", "attachment;filename="
					+ new String(filename.getBytes("GBK"), "iso-8859-1"));
			response.setCharacterEncoding("UTF-8");
			OutputStream ouputStream = response.getOutputStream();
			wb.write(ouputStream);
			ouputStream.flush();
			ouputStream.close();

		} catch (ExcelException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 报到进度统计
	 * 
	 * @param model
	 * @param request
	 * @param yearDic
	 * @param range
	 * @return
	 */
	@RequestMapping(value = { Constants.REPORT_PROGRESS
			+ "/opt-query/countReportProgress" })
	public String countReportProgress(ModelMap model,
			HttpServletRequest request, Dic yearDic, String range,
			String collegeId, String majorId, String klassId) {
		log.info("报到进度统计");
		// 添加学院条件
		String defaultCollegeId = ProjectSessionUtils
				.getCurrentTeacherOrgId(request);
		if (CheckUtils.isCurrentOrgEqCollege(defaultCollegeId)) {
			collegeId = defaultCollegeId;
			model.addAttribute("flag", true);
		} else
			defaultCollegeId = null;
		// 如果为空，默认当前学年
		if (yearDic == null || yearDic.getId() == null
				|| yearDic.getId().equals(""))
			yearDic = currentYearDic();
		else
			yearDic = dicService.getDic(yearDic.getId());

		if (range == null)
			range = "1";

		List<String> provinceList = regulatoryAnalysisService
				.getProvinceDicByEnterYear(yearDic);
		List<Dic> provinceDicList = new ArrayList<Dic>();
		for (String s : provinceList) {
			provinceDicList
					.add(dicUtil.getDicInfo("DIC_CANDIDATE_PROVENCE", s)); // 这个地方修改为学生的考生号所属的省份
		}
		// 学年
		model.addAttribute("yearList", dicUtil.getDicInfoList("YEAR"));
		model.addAttribute("yearDic", yearDic);
		model.addAttribute("range", range);

		model.addAttribute("collegeId", collegeId);
		model.addAttribute("majorId", majorId);
		model.addAttribute("klassId", klassId);

		// 学院处理方法 传到前台页面的列表
		List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		model.addAttribute("collegeList", collegeList);
		// 传到前台的专业列表
		if (DataUtil.isNotNull(collegeId)) {
			List<BaseMajorModel> listMajor = compService
					.queryMajorByCollage(collegeId);
			model.addAttribute("listMajor", listMajor);
		}
		// 传到前台的班级列表
		if (DataUtil.isNotNull(majorId)) {
			List<BaseClassModel> listKlass = compService
					.queryClassByMajor(majorId);
			model.addAttribute("listKlass", listKlass);
		}
		List<Object[]> pl = regulatoryAnalysisService
				.getReportProgressCountVoByProDic(yearDic.getId(), range,
						provinceDicList, collegeId, majorId, klassId);
		List<ReportProgressCountVo> reportProgressCountList = new ArrayList<ReportProgressCountVo>();
		HashMap<Integer, Long> mapTotal = new LinkedHashMap<Integer, Long>();
		// 录取人数合计
		Long totalEnterNum = 0L;
		// 报到人数合计
		Long totalRegisterNum = 0L;
		// 返回页面的名字
		String returnUrl = "";
		if ("1".equals(range)) {
			log.info("页面按学院统计");
			returnUrl = "reportProgressCollege";
			for (int i = 0; i < pl.size(); i++) {
				Object[] o = pl.get(i);
				ReportProgressCountVo r = new ReportProgressCountVo();
				r.setCollege((String) o[0]);
				r.setEnterNum(Long.parseLong(o[1].toString()));
				// 累加录取人数
				totalEnterNum += Long.parseLong(o[1].toString());
				r.setRegisterNum(Long.parseLong(o[2].toString()));
				// 累加报到人数
				totalRegisterNum += Long.parseLong(o[2].toString());
				Long[] provinceCountList = new Long[o.length - 3];
				for (int j = 3; j < o.length; j++) {
					provinceCountList[j - 3] = Long.parseLong(o[j].toString());
					if (mapTotal.get(Integer.valueOf(j)) != null)
						mapTotal.put(
								Integer.valueOf(j),
								mapTotal.get(Integer.valueOf(j))
										+ Long.parseLong(o[j].toString()));
					else
						mapTotal.put(Integer.valueOf(j),
								Long.parseLong(o[j].toString()));
				}
				r.setProvinceList(provinceCountList);
				reportProgressCountList.add(r);
			}
		} else if ("2".equals(range)) {
			log.info("页面按专业统计");
			returnUrl = "reportProgressMajor";
			for (int i = 0; i < pl.size(); i++) {
				Object[] o = pl.get(i);
				ReportProgressCountVo r = new ReportProgressCountVo();
				r.setCollege((String) o[0]);
				r.setMajor((String) o[1]);
				r.setEnterNum(Long.parseLong(o[2].toString()));
				// 累加录取人数
				totalEnterNum += Long.parseLong(o[2].toString());
				r.setRegisterNum(Long.parseLong(o[3].toString()));
				// 累加报到人数
				totalRegisterNum += Long.parseLong(o[3].toString());
				Long[] provinceCountList = new Long[o.length - 4];
				for (int j = 4; j < o.length; j++) {
					provinceCountList[j - 4] = Long.parseLong(o[j].toString());
					if (mapTotal.get(Integer.valueOf(j)) != null)
						mapTotal.put(
								Integer.valueOf(j),
								mapTotal.get(Integer.valueOf(j))
										+ Long.parseLong(o[j].toString()));
					else
						mapTotal.put(Integer.valueOf(j),
								Long.parseLong(o[j].toString()));
				}
				r.setProvinceList(provinceCountList);
				reportProgressCountList.add(r);
			}
		} else {
			log.info("页面按班级统计");
			returnUrl = "reportProgressClass";
			for (int i = 0; i < pl.size(); i++) {
				Object[] o = pl.get(i);
				ReportProgressCountVo r = new ReportProgressCountVo();
				r.setCollege((String) o[0]);
				r.setMajor((String) o[1]);
				r.setKlass((String) o[2]);
				r.setEnterNum(Long.parseLong(o[3].toString()));
				// 累加录取人数
				totalEnterNum += Long.parseLong(o[3].toString());
				r.setRegisterNum(Long.parseLong(o[4].toString()));
				// 累加报到人数
				totalRegisterNum += Long.parseLong(o[4].toString());
				Long[] provinceCountList = new Long[o.length - 5];
				for (int j = 5; j < o.length; j++) {
					provinceCountList[j - 5] = Long.parseLong(o[j].toString());
					if (mapTotal.get(Integer.valueOf(j)) != null)
						mapTotal.put(
								Integer.valueOf(j),
								mapTotal.get(Integer.valueOf(j))
										+ Long.parseLong(o[j].toString()));
					else
						mapTotal.put(Integer.valueOf(j),
								Long.parseLong(o[j].toString()));
				}
				r.setProvinceList(provinceCountList);
				reportProgressCountList.add(r);
			}
		}
		List<Long> totalList = new ArrayList<Long>();
		for (Map.Entry<Integer, Long> entry : mapTotal.entrySet()) {
			totalList.add(entry.getValue());
		}
		model.addAttribute("totalList", totalList);
		model.addAttribute("totalEnterNum", totalEnterNum);
		model.addAttribute("totalRegisterNum", totalRegisterNum);
		model.addAttribute("provinceDicList", provinceDicList);
		model.addAttribute("reportProgressCountList", reportProgressCountList);
		return Constants.REGULATORY_ANALYSIS + "/reportProgress/" + returnUrl;
	}

	/**
	 * 进入报到进度统计导出页面
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = Constants.REPORT_PROGRESS
			+ "/nsm/exportReportProgressView")
	public String exportReportProgressView(ModelMap model,
			HttpServletRequest request) {

		log.info("进入报到进度统计导出页面");
		String size = request.getParameter("exportSize");
		String page = request.getParameter("pageTotalCount");
		int exportSize = Integer.valueOf(
				(size != null && !size.equals("")) ? size : "1").intValue();
		int pageTotalCount = Integer.valueOf(
				(page != null && !page.equals("")) ? page : "1").intValue();
		int maxNumber = 0;
		if (pageTotalCount < exportSize) {
			maxNumber = 1;
		} else if (pageTotalCount % exportSize == 0) {
			maxNumber = pageTotalCount / exportSize;
		} else {
			maxNumber = pageTotalCount / exportSize + 1;
		}
		model.addAttribute("exportSize", Integer.valueOf(exportSize));
		model.addAttribute("maxNumber", Integer.valueOf(maxNumber));
		// 为了能将导出的数据效率高，判断每次导出数据500条
		if (maxNumber < 500) {
			model.addAttribute("isMore", "false");
		} else {
			model.addAttribute("isMore", "true");
		}
		return Constants.REGULATORY_ANALYSIS + "/exportReportProgressView";
	}

	/**
	 * 报到进度导出统计方法
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @param yearDic
	 * @param range
	 * @throws ParseException
	 */
	@RequestMapping(value = Constants.REPORT_PROGRESS
			+ "/opt-export/exportReportProgress")
	public void exportReportProgress(ModelMap model,
			HttpServletRequest request, HttpServletResponse response,
			Dic yearDic, String range, String collegeId, String majorId,
			String klassId) throws ParseException {
		log.info("导出报到进度统计方法");
		// 添加学院条件
		String defaultCollegeId = ProjectSessionUtils
				.getCurrentTeacherOrgId(request);
		if (CheckUtils.isCurrentOrgEqCollege(defaultCollegeId)) {
			collegeId = defaultCollegeId;
			model.addAttribute("flag", true);
		} else
			defaultCollegeId = null;

		String exportSize = request
				.getParameter("countReportProgress_exportSize");
		String exportPage = request
				.getParameter("countReportProgress_exportPage");

		// 如果为空，默认当前学年
		if (yearDic == null || yearDic.getId() == null
				|| yearDic.getId().equals(""))
			yearDic = currentYearDic();
		else
			yearDic = dicService.getDic(yearDic.getId());
		// 如果没有选择统计范围，按照学院统计
		if (range == null)
			range = "1";

		List<String> provinceList = regulatoryAnalysisService
				.getProvinceDicByEnterYear(yearDic);
		List<Dic> provinceDicList = new ArrayList<Dic>();
		List<String> provinceNameList = new ArrayList<String>();
		for (String s : provinceList) {
			Dic d = dicUtil.getDicInfo("DIC_CANDIDATE_PROVENCE", s);
			provinceDicList.add(d);
			provinceNameList.add(d.getName() + "(录取)");
			provinceNameList.add(d.getName() + "(报到)");
		}
		List<Object[]> pl = regulatoryAnalysisService
				.getReportProgressCountVoByProDic(yearDic.getId(), range,
						provinceDicList, collegeId, majorId, klassId);
		// 导出类封装
		HSSFWorkbook wb = new HSSFWorkbook();

		// 生成列标题样式
		HSSFCellStyle styleTitle = wb.createCellStyle();
		styleTitle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		styleTitle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleTitle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleTitle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleTitle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleTitle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleTitle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		styleTitle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		// 生成内容字体
		HSSFFont fontTitle = wb.createFont();
		fontTitle.setFontHeightInPoints((short) 11);
		fontTitle.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		styleTitle.setFont(fontTitle);

		String fileName = yearDic.getName();
		if ("1".equals(range)) {
			fileName += "按学院统计报到进度表.xls";
			HSSFSheet sheet = wb.createSheet("按学院统计报到进度");

			List<String> titleList = new ArrayList<String>();
			titleList.add("入学年份");
			titleList.add("二级学院");
			titleList.add("录取人数");
			titleList.add("报到人数");
			titleList.add("学院报到率");
			titleList.addAll(provinceNameList);

			HSSFRow row = sheet.createRow(0);
			row.setRowStyle(styleTitle);
			row.setHeight((short) (500));
			for (int i = 0; i < titleList.size(); i++) {
				HSSFCell cell = row.createCell(i);
				cell.setCellValue(titleList.get(i));
				cell.setCellStyle(styleTitle);
				if (i == 0 || i == 2 || i == 3)
					sheet.setColumnWidth(i, 256 * 11);
				else if (i == 4)
					sheet.setColumnWidth(i, 256 * 12);
				else if (titleList.get(i).length() > 10)
					sheet.setColumnWidth(i, 256 * 25);
				else
					sheet.setColumnWidth(i, 256 * 15);
			}

			LinkedHashMap<Integer, Long> mapTotal = new LinkedHashMap<Integer, Long>();

			for (int i = 0; i < pl.size(); i++) {
				HSSFRow newRow = sheet.createRow(i + 1);
				newRow.createCell(0).setCellValue(yearDic.getName());
				int k = 1;
				for (int j = 0; j < pl.get(i).length; j++) {
					if (j == 0) {
						newRow.createCell(k++).setCellValue(
								(String) pl.get(i)[j]);
					} else if (j == 3) {
						newRow.createCell(k++)
								.setCellValue(
										percentFormat(
												Long.parseLong(pl.get(i)[2]
														.toString()), Long
														.parseLong(pl.get(i)[1]
																.toString())));
						newRow.createCell(k++).setCellValue(
								Long.parseLong(pl.get(i)[j].toString()));
						if (mapTotal.get(Integer.valueOf(k)) != null)
							mapTotal.put(
									Integer.valueOf(k),
									mapTotal.get(Integer.valueOf(k))
											+ Long.parseLong(pl.get(i)[j]
													.toString()));
						else
							mapTotal.put(Integer.valueOf(k),
									Long.parseLong(pl.get(i)[j].toString()));
					} else {
						newRow.createCell(k++).setCellValue(
								Long.parseLong(pl.get(i)[j].toString()));
						if (mapTotal.get(Integer.valueOf(k)) != null)
							mapTotal.put(
									Integer.valueOf(k),
									mapTotal.get(Integer.valueOf(k))
											+ Long.parseLong(pl.get(i)[j]
													.toString()));
						else
							mapTotal.put(Integer.valueOf(k),
									Long.parseLong(pl.get(i)[j].toString()));
					}
				}
			}
			// 合计行
			HSSFRow newTotalRow = sheet.createRow(pl.size() + 1);
			newTotalRow.createCell(1).setCellValue("合计");
			for (Map.Entry<Integer, Long> entry : mapTotal.entrySet()) {
				newTotalRow.createCell(entry.getKey() - 1).setCellValue(
						entry.getValue());
			}
			newTotalRow.createCell(4).setCellValue(
					percentFormat(mapTotal.get(4), mapTotal.get(3)));
			// 各省报到率行
			HSSFRow eveProvinceRow = sheet.createRow(pl.size() + 2);
			eveProvinceRow.createCell(1).setCellValue("各省报到率");
			for (Map.Entry<Integer, Long> entry : mapTotal.entrySet()) {
				if (entry.getKey() > 5 && (entry.getKey() % 2 == 0))
					eveProvinceRow.createCell(entry.getKey() - 1).setCellValue(
							percentFormat(mapTotal.get(entry.getKey() + 1),
									entry.getValue()));
			}
			// 外省报到率
			HSSFRow otherProvinceRow = sheet.createRow(pl.size() + 3);
			otherProvinceRow.createCell(1).setCellValue("外省报到率");
			List<Object[]> rrl = regulatoryAnalysisService
					.getOtherProvinceCount(yearDic.getId(), collegeId, majorId,
							klassId, "p");
			otherProvinceRow.createCell(2).setCellValue(
					percentFormat(
							null != rrl.get(0)[0] ? Long.valueOf(rrl.get(0)[0]
									.toString()) : 0,
							null != rrl.get(0)[1] ? Long.valueOf(rrl.get(0)[1]
									.toString()) : 0));
			// 学校总报到率
			HSSFRow stRow = sheet.createRow(pl.size() + 4);
			stRow.createCell(1).setCellValue("全校报到率");
			List<Object[]> stl = regulatoryAnalysisService
					.getOtherProvinceCount(yearDic.getId(), collegeId, majorId,
							klassId, null);
			stRow.createCell(2).setCellValue(
					percentFormat(
							null != stl.get(0)[0] ? Long.valueOf(stl.get(0)[0]
									.toString()) : 0,
							null != stl.get(0)[1] ? Long.valueOf(stl.get(0)[1]
									.toString()) : 0));
		} else if ("2".equals(range)) {
			fileName += "按专业统计报到进度表.xls";
			HSSFSheet sheet = wb.createSheet("按专业统计报到进度");

			List<String> titleList = new ArrayList<String>();
			titleList.add("入学年份");
			titleList.add("二级学院");
			titleList.add("专业");
			titleList.add("录取人数");
			titleList.add("报到人数");
			titleList.add("专业报到率");
			titleList.addAll(provinceNameList);

			HSSFRow row = sheet.createRow(0);
			row.setRowStyle(styleTitle);
			row.setHeight((short) (500));
			for (int i = 0; i < titleList.size(); i++) {
				HSSFCell cell = row.createCell(i);
				cell.setCellValue(titleList.get(i));
				cell.setCellStyle(styleTitle);
				if (i == 0 || i == 3 || i == 4)
					sheet.setColumnWidth(i, 256 * 11);
				else if (i == 5)
					sheet.setColumnWidth(i, 256 * 12);
				else if (titleList.get(i).length() > 10)
					sheet.setColumnWidth(i, 256 * 25);
				else
					sheet.setColumnWidth(i, 256 * 15);
			}

			LinkedHashMap<Integer, Long> mapTotal = new LinkedHashMap<Integer, Long>();

			for (int i = 0; i < pl.size(); i++) {
				HSSFRow newRow = sheet.createRow(i + 1);
				newRow.createCell(0).setCellValue(yearDic.getName());
				int k = 1;
				for (int j = 0; j < pl.get(i).length; j++) {
					if (j < 2) {
						newRow.createCell(k++).setCellValue(
								(String) pl.get(i)[j]);
					} else if (j == 4) {
						newRow.createCell(k++)
								.setCellValue(
										percentFormat(
												Long.parseLong(pl.get(i)[3]
														.toString()), Long
														.parseLong(pl.get(i)[2]
																.toString())));
						newRow.createCell(k++).setCellValue(
								Long.parseLong(pl.get(i)[j].toString()));
						if (mapTotal.get(Integer.valueOf(k)) != null)
							mapTotal.put(
									Integer.valueOf(k),
									mapTotal.get(Integer.valueOf(k))
											+ Long.parseLong(pl.get(i)[j]
													.toString()));
						else
							mapTotal.put(Integer.valueOf(k),
									Long.parseLong(pl.get(i)[j].toString()));
					} else {
						newRow.createCell(k++).setCellValue(
								Long.parseLong(pl.get(i)[j].toString()));
						if (mapTotal.get(Integer.valueOf(k)) != null)
							mapTotal.put(
									Integer.valueOf(k),
									mapTotal.get(Integer.valueOf(k))
											+ Long.parseLong(pl.get(i)[j]
													.toString()));
						else
							mapTotal.put(Integer.valueOf(k),
									Long.parseLong(pl.get(i)[j].toString()));
					}
				}
			}
			// 合计行
			HSSFRow newTotalRow = sheet.createRow(pl.size() + 1);
			newTotalRow.createCell(1).setCellValue("合计");
			for (Map.Entry<Integer, Long> entry : mapTotal.entrySet()) {
				newTotalRow.createCell(entry.getKey() - 1).setCellValue(
						entry.getValue());
			}
			newTotalRow.createCell(5).setCellValue(
					percentFormat(mapTotal.get(5), mapTotal.get(4)));
			// 各省报到率行
			HSSFRow eveProvinceRow = sheet.createRow(pl.size() + 2);
			eveProvinceRow.createCell(1).setCellValue("各省报到率");
			for (Map.Entry<Integer, Long> entry : mapTotal.entrySet()) {
				if (entry.getKey() > 6 && (entry.getKey() % 2 != 0))
					eveProvinceRow.createCell(entry.getKey() - 1).setCellValue(
							percentFormat(mapTotal.get(entry.getKey() + 1),
									entry.getValue()));
			}
			// 外省报到率
			HSSFRow otherProvinceRow = sheet.createRow(pl.size() + 3);
			otherProvinceRow.createCell(1).setCellValue("外省报到率");
			List<Object[]> rrl = regulatoryAnalysisService
					.getOtherProvinceCount(yearDic.getId(), collegeId, majorId,
							klassId, "p");
			otherProvinceRow.createCell(3).setCellValue(
					percentFormat(
							null != rrl.get(0)[0] ? Long.valueOf(rrl.get(0)[0]
									.toString()) : 0,
							null != rrl.get(0)[1] ? Long.valueOf(rrl.get(0)[1]
									.toString()) : 0));
			// 学校总报到率
			HSSFRow stRow = sheet.createRow(pl.size() + 4);
			stRow.createCell(1).setCellValue("全校报到率");
			List<Object[]> stl = regulatoryAnalysisService
					.getOtherProvinceCount(yearDic.getId(), collegeId, majorId,
							klassId, null);
			stRow.createCell(3).setCellValue(
					percentFormat(
							null != stl.get(0)[0] ? Long.valueOf(stl.get(0)[0]
									.toString()) : 0,
							null != stl.get(0)[1] ? Long.valueOf(stl.get(0)[1]
									.toString()) : 0));
		} else {
			fileName += "按班级统计报到进度表.xls";
			HSSFSheet sheet = wb.createSheet("按班级统计报到进度");

			List<String> titleList = new ArrayList<String>();
			titleList.add("入学年份");
			titleList.add("二级学院");
			titleList.add("专业");
			titleList.add("班级");
			titleList.add("录取人数");
			titleList.add("报到人数");
			titleList.add("班级报到率");
			titleList.addAll(provinceNameList);

			HSSFRow row = sheet.createRow(0);
			row.setRowStyle(styleTitle);
			row.setHeight((short) (500));
			for (int i = 0; i < titleList.size(); i++) {
				HSSFCell cell = row.createCell(i);
				cell.setCellValue(titleList.get(i));
				cell.setCellStyle(styleTitle);
				if (i == 0 || i == 4 || i == 5)
					sheet.setColumnWidth(i, 256 * 11);
				else if (i == 6)
					sheet.setColumnWidth(i, 256 * 12);
				else if (titleList.get(i).length() > 10)
					sheet.setColumnWidth(i, 256 * 25);
				else
					sheet.setColumnWidth(i, 256 * 15);
			}
			LinkedHashMap<Integer, Long> mapTotal = new LinkedHashMap<Integer, Long>();

			for (int i = 0; i < pl.size(); i++) {
				HSSFRow newRow = sheet.createRow(i + 1);
				newRow.createCell(0).setCellValue(yearDic.getName());
				int k = 1;
				for (int j = 0; j < pl.get(i).length; j++) {
					if (j < 3) {
						newRow.createCell(k++).setCellValue(
								(String) pl.get(i)[j]);
					} else if (j == 5) {
						newRow.createCell(k++)
								.setCellValue(
										percentFormat(
												Long.parseLong(pl.get(i)[4]
														.toString()), Long
														.parseLong(pl.get(i)[3]
																.toString())));
						newRow.createCell(k++).setCellValue(
								Long.parseLong(pl.get(i)[j].toString()));
						if (mapTotal.get(Integer.valueOf(k)) != null)
							mapTotal.put(
									Integer.valueOf(k),
									mapTotal.get(Integer.valueOf(k))
											+ Long.parseLong(pl.get(i)[j]
													.toString()));
						else
							mapTotal.put(Integer.valueOf(k),
									Long.parseLong(pl.get(i)[j].toString()));
					} else {
						newRow.createCell(k++).setCellValue(
								Long.parseLong(pl.get(i)[j].toString()));
						if (mapTotal.get(Integer.valueOf(k)) != null)
							mapTotal.put(
									Integer.valueOf(k),
									mapTotal.get(Integer.valueOf(k))
											+ Long.parseLong(pl.get(i)[j]
													.toString()));
						else
							mapTotal.put(Integer.valueOf(k),
									Long.parseLong(pl.get(i)[j].toString()));
					}
				}
			}
			// 合计行
			HSSFRow newTotalRow = sheet.createRow(pl.size() + 1);
			newTotalRow.createCell(1).setCellValue("合计");
			for (Map.Entry<Integer, Long> entry : mapTotal.entrySet()) {
				newTotalRow.createCell(entry.getKey() - 1).setCellValue(
						entry.getValue());
			}
			newTotalRow.createCell(6).setCellValue(
					percentFormat(mapTotal.get(6), mapTotal.get(5)));
			// 各省报到率行
			HSSFRow eveProvinceRow = sheet.createRow(pl.size() + 2);
			eveProvinceRow.createCell(1).setCellValue("各省报到率");
			for (Map.Entry<Integer, Long> entry : mapTotal.entrySet()) {
				if (entry.getKey() > 7 && (entry.getKey() % 2 == 0))
					eveProvinceRow.createCell(entry.getKey() - 1).setCellValue(
							percentFormat(mapTotal.get(entry.getKey() + 1),
									entry.getValue()));
			}
			// 外省报到率
			HSSFRow otherProvinceRow = sheet.createRow(pl.size() + 3);
			otherProvinceRow.createCell(1).setCellValue("外省报到率");
			List<Object[]> rrl = regulatoryAnalysisService
					.getOtherProvinceCount(yearDic.getId(), collegeId, majorId,
							klassId, "p");
			otherProvinceRow.createCell(4).setCellValue(
					percentFormat(
							null != rrl.get(0)[0] ? Long.valueOf(rrl.get(0)[0]
									.toString()) : 0,
							null != rrl.get(0)[1] ? Long.valueOf(rrl.get(0)[1]
									.toString()) : 0));
			// 学校总报到率
			HSSFRow stRow = sheet.createRow(pl.size() + 4);
			stRow.createCell(1).setCellValue("全校报到率");
			List<Object[]> stl = regulatoryAnalysisService
					.getOtherProvinceCount(yearDic.getId(), collegeId, majorId,
							klassId, null);
			stRow.createCell(4).setCellValue(
					percentFormat(
							null != stl.get(0)[0] ? Long.valueOf(stl.get(0)[0]
									.toString()) : 0,
							null != stl.get(0)[1] ? Long.valueOf(stl.get(0)[1]
									.toString()) : 0));
		}
		try {
			response.setContentType("application/x-excel");
			response.setHeader("Content-disposition", "attachment;filename="
					+ new String(fileName.getBytes("GBK"), "iso-8859-1"));
			response.setCharacterEncoding("UTF-8");
			OutputStream ouputStream = response.getOutputStream();
			wb.write(ouputStream);
			ouputStream.flush();
			ouputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 各报到点统计
	 * 
	 * @param model
	 * @param request
	 * @param yearDic
	 * @return
	 */
	@RequestMapping(value = { Constants.REPORT_PLACE
			+ "/opt-query/countReportPlace" })
	public String countReportPlace(ModelMap model, HttpServletRequest request,
			Dic yearDic) {
		log.info("各报到点统计");

		List<Dic> reportSites = dicUtil.getDicInfoList("REPORT_SITE");
		List<PlaceCountVo> listPlaceCounts = new ArrayList<PlaceCountVo>();
		Integer TOTAL_NUM = 0;
		if (yearDic == null || yearDic.getId() == null
				|| yearDic.getId().equals(""))
			yearDic = currentYearDic();

		for (Dic reportSite : reportSites) {
			List<StudentInfoModel> list = regulatoryAnalysisService
					.getAllStudentInfo(yearDic, reportSite);
			TOTAL_NUM += ((list != null) ? list.size() : 0);
			PlaceCountVo placeCountVo = new PlaceCountVo();
			placeCountVo.setPlace(reportSite);
			placeCountVo.setNum(list != null ? list.size() : 0);
			listPlaceCounts.add(placeCountVo);
		}
		model.addAttribute("TOTAL_NUM", TOTAL_NUM);

		model.addAttribute("listPlaceCounts", listPlaceCounts);
		model.addAttribute("yearDic", yearDic);

		// 学年字典
		model.addAttribute("yearList", dicUtil.getDicInfoList("YEAR"));

		return Constants.REGULATORY_ANALYSIS + "/reportPlace";
	}

	/**
	 * 绿色通到统计
	 * 
	 * @param model
	 * @param request
	 * @param reportProgressVo
	 * @return
	 */
	@RequestMapping(value = { Constants.GREEN_CHANNEL
			+ "/opt-query/countGreenChannel" })
	public String countGreenChannel(ModelMap model, HttpServletRequest request,
			ReportProgressVo reportProgressVo) {
		log.info("绿色通到统计");
		// 添加学院条件
		String defaultCollegeId = ProjectSessionUtils
				.getCurrentTeacherOrgId(request);
		if (CheckUtils.isCurrentOrgEqCollege(defaultCollegeId)) {
			reportProgressVo.setCollege(defaultCollegeId);
			model.addAttribute("flag", true);
		}
		// 默认当前学年字典
		if (reportProgressVo == null || reportProgressVo.getYearDic() == null
				|| reportProgressVo.getYearDic().getId() == null
				|| reportProgressVo.getYearDic().getId().equals("")) {
			reportProgressVo.setYearDic(currentYearDic());
		}
		// 学院
		List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		// 专业
		List<BaseMajorModel> majorList = null;
		if (null != reportProgressVo && reportProgressVo.getCollege() != null
				&& !reportProgressVo.getCollege().equals("")) {
			majorList = compService.queryMajorByCollage(reportProgressVo
					.getCollege());
			log.debug("若已经选择学院，则查询学院下的专业信息.");
		}
		// 班级
		List<BaseClassModel> classList = null;
		if (null != reportProgressVo && reportProgressVo.getMajorId() != null
				&& !reportProgressVo.getMajorId().equals("")) {
			classList = compService.queryClassByMajor(reportProgressVo
					.getMajorId());
		}

		// 查询绿色通到数据
		Integer pageNo = request.getParameter("pageNo") != null ? Integer
				.valueOf(request.getParameter("pageNo")) : 1;
		Page page = regulatoryAnalysisService.queryPageGreenChannel(
				Page.DEFAULT_PAGE_SIZE, pageNo, reportProgressVo, "green");
		// 符合查询条件的绿色通到的人数
		Long greenNum = page.getTotalCount();

		// 符合查询条件的所有报到人数
		Page pageTotal = regulatoryAnalysisService.queryPageGreenChannel(
				Page.DEFAULT_PAGE_SIZE, pageNo, reportProgressVo, null);
		Long totalNum = pageTotal.getTotalCount();

		// 学年字典
		model.addAttribute("yearList", dicUtil.getDicInfoList("YEAR"));
		// 学院
		model.addAttribute("collegeList", collegeList);
		// 专业
		model.addAttribute("majorList", majorList);
		// 班级
		model.addAttribute("classList", classList);

		model.addAttribute("page", page);
		model.addAttribute("greenNum", greenNum);
		model.addAttribute("totalNum", totalNum);

		model.addAttribute("reportProgress", reportProgressVo);
		return Constants.REGULATORY_ANALYSIS + "/greenChannel";
	}

	/**
	 * 进入绿色通到导出页面
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = Constants.GREEN_CHANNEL
			+ "/nsm/exportCountExReportView")
	public String exportGreenChannelView(ModelMap model,
			HttpServletRequest request) {
		log.info("进入导出页面");
		String size = request.getParameter("exportSize");
		String page = request.getParameter("pageTotalCount");
		int exportSize = Integer.valueOf(
				(size != null && !size.equals("")) ? size : "1").intValue();
		int pageTotalCount = Integer.valueOf(
				(page != null && !page.equals("")) ? page : "1").intValue();
		int maxNumber = 0;
		if (pageTotalCount < exportSize) {
			maxNumber = 1;
		} else if (pageTotalCount % exportSize == 0) {
			maxNumber = pageTotalCount / exportSize;
		} else {
			maxNumber = pageTotalCount / exportSize + 1;
		}
		model.addAttribute("exportSize", Integer.valueOf(exportSize));
		model.addAttribute("maxNumber", Integer.valueOf(maxNumber));
		// 为了能将导出的数据效率高，判断每次导出数据500条
		if (maxNumber < 500) {
			model.addAttribute("isMore", "false");
		} else {
			model.addAttribute("isMore", "true");
		}
		return Constants.REGULATORY_ANALYSIS + "/exportGreenChannelView";
	}

	/**
	 * 导出绿色通到数据
	 * 
	 * @param model
	 * @param request
	 * @param demoPo
	 * @param response
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = Constants.GREEN_CHANNEL
			+ "/opt-export/exportGreenChannel")
	public void exportGreenChannel(ModelMap model, HttpServletRequest request,
			HttpServletResponse response, ReportProgressVo reportProgressVo) {
		log.info("导出绿色通到表方法");
		// 添加学院条件
		String defaultCollegeId = ProjectSessionUtils
				.getCurrentTeacherOrgId(request);
		if (CheckUtils.isCurrentOrgEqCollege(defaultCollegeId)) {
			reportProgressVo.setCollege(defaultCollegeId);
		}
		String exportSize = request
				.getParameter("greenChannelQuery_exportSize");
		String exportPage = request
				.getParameter("greenChannelQuery_exportPage");
		// 默认当前学年字典
		if (reportProgressVo == null || reportProgressVo.getYearDic() == null
				|| reportProgressVo.getYearDic().getId() == null
				|| reportProgressVo.getYearDic().getId().equals("")) {
			reportProgressVo.setYearDic(currentYearDic());
		}

		Page page = regulatoryAnalysisService.queryPageGreenChannel(
				(exportSize != null && !exportSize.equals("")) ? Integer
						.parseInt(exportSize) : 1,
				(exportPage != null && !exportPage.equals("")) ? Integer
						.parseInt(exportPage) : 1, reportProgressVo, "green");

		List<StudentInfoModel> list = (List<StudentInfoModel>) page.getResult();

		List<Map> listMap = new ArrayList<Map>();

		int index = 1;
		// 遍历要导出的数据，并将数据放入map对象中
		for (StudentInfoModel s : list) {
			Map<String, Object> newmap = new HashMap<String, Object>();
			// 序号
			newmap.put("indexs", String.valueOf(index));
			newmap.put("status", "已报到");
			// 学院
			newmap.put("college",
					(s.getCollege() != null && s.getCollege() != null && !s
							.getCollege().equals("")) ? s.getCollege()
							.getName() : "");
			newmap.put("major", (s.getMajor() != null) ? s.getMajor()
					.getMajorName() : "");
			newmap.put("className", (s != null && s.getClassId() != null) ? s
					.getClassId().getClassName() : "");
			newmap.put("dormName",
					(s != null && s.getDorm() != null) ? s.getDorm() : "");
			newmap.put("stuNumber",
					(s != null && s.getStuNumber() != null) ? s.getStuNumber()
							: "");
			newmap.put("name", (s != null && s.getName() != null) ? s.getName()
					: "");
			newmap.put("namePy",
					(s != null && s.getNamePy() != null) ? s.getNamePy() : "");
			newmap.put(
					"englishName",
					(s != null && s.getEnglishName() != null) ? s
							.getEnglishName() : "");
			newmap.put("oldName",
					(s != null && s.getOldName() != null) ? s.getOldName() : "");
			newmap.put("sex",
					(s != null && s.getGenderDic() != null
							&& s.getGenderDic() != null && !s.getGenderDic()
							.equals("")) ? s.getGenderDic().getName() : "");
			newmap.put(
					"birthDate",
					(s != null && s.getBrithDateStr() != null) ? s
							.getBrithDateStr() : "");
			newmap.put(
					"cerCode",
					(s != null && s.getCertificateCode() != null) ? s
							.getCertificateCode() : "");
			newmap.put("political", (s != null && s.getPoliticalDic() != null
					&& s.getPoliticalDic().getId() != null && !s
					.getPoliticalDic().getId().equals("")) ? s
					.getPoliticalDic().getName() : "");
			// 民族
			newmap.put("nation",
					(s != null && s.getNational() != null) ? s.getNational()
							: "");
			// 毕业学校
			newmap.put(
					"graduation",
					(s != null && s.getGraduation() != null) ? s
							.getGraduation() : "");
			// 录取总分
			newmap.put(
					"enterScore",
					(s != null && s.getEnterScore() != null) ? s
							.getEnterScore() : "");
			// 生源地
			newmap.put(
					"sorceLand",
					(s != null && s.getSourceLand() != null) ? s
							.getSourceLand() : "");
			// 籍贯
			newmap.put("native", (s != null && s.getNativeDic() != null
					&& s.getNativeDic().getId() != null && !s.getNativeDic()
					.getId().equals("")) ? s.getNativeDic().getName() : "");
			// 户口类别
			newmap.put("addressType", (s != null
					&& s.getAddressTypeDic() != null
					&& s.getAddressTypeDic().getId() != null && !s
					.getAddressTypeDic().getId().equals("")) ? s
					.getAddressTypeDic().getName() : "");
			// 户口地址
			newmap.put("nativeAddress",
					(s != null && s.getNativeAdd() != null) ? s.getNativeAdd()
							: "");
			// 家庭地址
			newmap.put(
					"homeAddress",
					(s != null && s.getHomeAddress() != null) ? s
							.getHomeAddress() : "");
			// 家庭邮政编码
			newmap.put(
					"homePostCode",
					(s != null && s.getHomePostCode() != null) ? s
							.getHomePostCode() : "");
			// 家庭电话
			newmap.put("homeTel",
					(s != null && s.getHomeTel() != null) ? s.getHomeTel() : "");
			// 手机号码1
			newmap.put("phone1",
					(s != null && s.getPhone1() != null) ? s.getPhone1() : "");
			// 手机号码2
			newmap.put("phone2",
					(s != null && s.getPhone2() != null) ? s.getPhone2() : "");
			// 电子邮箱
			newmap.put("email",
					(s != null && s.getEmail() != null) ? s.getEmail() : "");
			// QQ
			newmap.put("indexS", (s != null && s.getQq() != null) ? s.getQq()
					: "");
			// 网络地址
			newmap.put("url",
					(s != null && s.getUrlStr() != null) ? s.getUrlStr() : "");
			// 婚姻状况
			newmap.put("marriage", (s != null && s.getMarriageDic() != null
					&& s.getMarriageDic().getId() != null && !s
					.getMarriageDic().getId().equals("")) ? s.getMarriageDic()
					.getName() : "");
			// 港澳台侨
			newmap.put("overChinese", (s != null
					&& s.getOverChineseDic() != null
					&& s.getOverChineseDic().getId() != null && !s
					.getOverChineseDic().getId().equals("")) ? s
					.getOverChineseDic().getName() : "");
			// 宗教信仰
			newmap.put("religion", (s != null && s.getReligionDic() != null
					&& s.getReligionDic().getId() != null && !s
					.getReligionDic().getId().equals("")) ? s.getReligionDic()
					.getName() : "");
			// 健康状况
			newmap.put("health", (s != null && s.getHealthStateDic() != null
					&& s.getHealthStateDic().getId() != null && !s
					.getHealthStateDic().getId().equals("")) ? s
					.getHealthStateDic().getName() : "");
			// 血型
			newmap.put("bloodType", (s != null && s.getBloodTypeDic() != null
					&& s.getBloodTypeDic().getId() != null && !s
					.getBloodTypeDic().getId().equals("")) ? s
					.getBloodTypeDic().getName() : "");
			// 银行卡号
			newmap.put("bankCode",
					(s != null && s.getBankCode() != null) ? s.getBankCode()
							: "");
			// 入党申请
			newmap.put("partyApp",
					(s != null && s.getPartyApp() != null) ? s.getPartyApp()
							: "");
			// 党校学习
			newmap.put(
					"partyStudy",
					(s != null && s.getPartyStudy() != null) ? s
							.getPartyStudy() : "");
			// 缴费状况
			newmap.put("costState",
					(s != null && s.getCostState() != null) ? s.getCostState()
							: "");
			// 绿色通到
			newmap.put("greenWay",
					(s != null && s.getGreenWay() != null) ? s.getGreenWay()
							: "");
			// 绿色通到原因
			newmap.put("greenReason", (s != null && s.getGreenReason() != null
					&& s.getGreenReason().getId() != null && !s
					.getGreenReason().getId().equals("")) ? s.getGreenReason()
					.getName() : "");
			// 撤销原因
			newmap.put(
					"cancelReason",
					(s != null && s.getCancelReason() != null) ? s
							.getCancelReason() : "");
			// 入学年份
			newmap.put("enterYear", (s != null && s.getEnterYearDic() != null
					&& s.getEnterYearDic().getId() != null && !s
					.getEnterYearDic().getId().equals("")) ? s
					.getEnterYearDic().getName() : "");
			// 报到时间
			newmap.put(
					"reportDate",
					(s != null && s.getReportDate() != null) ? s
							.getReportDate() : "");
			// 报到地点
			newmap.put("reportSite", (s != null && s.getReportSiteDic() != null
					&& s.getReportSiteDic().getId() != null && !s
					.getReportSiteDic().getId().equals("")) ? s
					.getReportSiteDic().getName() : "");
			// 新生采集状态
			newmap.put(
					"collectState",
					(s != null && s.getCollectState() != null) ? s
							.getCollectState() : "");

			// 监护人类
			StudentGuardianModel sgm = studentGuardianService.getByStudentId(s
					.getId());
			if (sgm != null) {
				// 父亲姓名
				newmap.put(
						"fatherName",
						(sgm.getFatherName() != null ? sgm.getFatherName() : ""));
				// 父亲手机号码
				newmap.put("fatherPhone",
						(sgm.getFatherPhone() != null ? sgm.getFatherPhone()
								: ""));
				// 父亲邮箱
				newmap.put("fatherEmail",
						(sgm.getFatherEmail() != null ? sgm.getFatherEmail()
								: ""));
				// 父亲住址
				newmap.put(
						"fatherAddress",
						(sgm.getFatherAddress() != null ? sgm
								.getFatherAddress() : ""));
				// 父亲邮编
				newmap.put(
						"fatherPostCode",
						(sgm.getFatherPostCode() != null ? sgm
								.getFatherPostCode() : ""));
				// 父亲工作单位
				newmap.put(
						"fatherWorkUnit",
						(sgm.getFatherWorkUnit() != null ? sgm
								.getFatherWorkUnit() : ""));
				// 母亲姓名
				newmap.put(
						"motherName",
						(sgm.getMotherName() != null ? sgm.getMotherName() : ""));
				// 母亲手机号码
				newmap.put("motherPhone",
						(sgm.getMotherPhone() != null ? sgm.getMotherPhone()
								: ""));
				// 母亲邮箱
				newmap.put("motherEmail",
						(sgm.getMotherEmail() != null ? sgm.getMotherEmail()
								: ""));
				// 母亲住址
				newmap.put(
						"motherAddress",
						(sgm.getMotherAddress() != null ? sgm
								.getMotherAddress() : ""));
				// 母亲邮编
				newmap.put(
						"motherPostCode",
						(sgm.getMotherPostCode() != null ? sgm
								.getMotherPostCode() : ""));
				// 母亲工作单位
				newmap.put(
						"motherWorkUnit",
						(sgm.getMotherWorkUnit() != null ? sgm
								.getMotherWorkUnit() : ""));
				// 监护人姓名
				newmap.put("guardianName",
						(sgm.getGuardianName() != null ? sgm.getGuardianName()
								: ""));
				// 监护人手机号码
				newmap.put(
						"guardianPhone",
						(sgm.getGuardianPhone() != null ? sgm
								.getGuardianPhone() : ""));
				// 监护人邮箱
				newmap.put(
						"guardianEmail",
						(sgm.getGuardianEmail() != null ? sgm
								.getGuardianEmail() : ""));
				// 监护人住址
				newmap.put(
						"guardianAddress",
						(sgm.getGuardianAddress() != null ? sgm
								.getGuardianAddress() : ""));
				// 监护人邮编
				newmap.put(
						"guardianPostCode",
						(sgm.getGuardianPostCode() != null ? sgm
								.getGuardianPostCode() : ""));
				// 监护人工作单位
				newmap.put(
						"guardianWorkUnit",
						(sgm.getGuardianWorkUnit() != null ? sgm
								.getGuardianWorkUnit() : ""));

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
			index++;
			listMap.add(newmap);
		}

		try {
			HSSFWorkbook wb = excelService.exportData(
					"export_student_info.xls", "studentInfoExport", listMap);
			String filename = "绿色通到学生表"
					+ (exportPage != null ? exportPage : "") + ".xls";
			response.setContentType("application/x-excel");
			response.setHeader("Content-disposition", "attachment;filename="
					+ new String(filename.getBytes("GBK"), "iso-8859-1"));
			response.setCharacterEncoding("UTF-8");
			OutputStream ouputStream = response.getOutputStream();
			wb.write(ouputStream);
			ouputStream.flush();
			ouputStream.close();

		} catch (ExcelException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 院系缴费统计
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { Constants.REPORT_PAYMENT
			+ "/opt-query/countPayment" })
	public String countPayment(ModelMap model, HttpServletRequest request) {
		log.info("院系缴费统计");
		List<PaidVo> listPaidVo = new ArrayList<PaidVo>();
		// 学院
		List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		// 添加学院条件
		String defaultCollegeId = ProjectSessionUtils
				.getCurrentTeacherOrgId(request);
		if (CheckUtils.isCurrentOrgEqCollege(defaultCollegeId)) {
			collegeList.clear();
			collegeList.add(baseDataService.findAcademyById(defaultCollegeId));
			model.addAttribute("flag", true);
		}
		// 录取总人数合计
		Integer totalNum = 0;
		// 已缴费人数合计
		Integer paidNum = 0;
		// 未缴费人数
		Integer unpaidNum = 0;
		// 绿色通道人数
		Integer greenNum = 0;

		for (BaseAcademyModel college : collegeList) {
			PaidVo paidVo = new PaidVo();
			paidVo.setCollege(college);
			// 已缴费
			List<StudentInfoModel> listPaid = regulatoryAnalysisService
					.queryCostState(currentYearDic(), college, "paid");
			paidVo.setPaid((listPaid != null) ? listPaid.size() : 0);
			paidNum += (listPaid != null) ? listPaid.size() : 0;
			// 未交费
			List<StudentInfoModel> listUnpaid = regulatoryAnalysisService
					.queryCostState(currentYearDic(), college, "unpaid");
			paidVo.setUnpaid((listUnpaid != null) ? listUnpaid.size() : 0);
			unpaidNum += (listUnpaid != null) ? listUnpaid.size() : 0;
			// 绿色通到
			List<StudentInfoModel> listGreen = regulatoryAnalysisService
					.queryCostState(currentYearDic(), college, "green");
			paidVo.setGreen((listGreen != null) ? listGreen.size() : 0);
			greenNum += (listGreen != null) ? listGreen.size() : 0;
			// 录取总人数
			List<StudentInfoModel> listTotal = regulatoryAnalysisService
					.queryCostState(currentYearDic(), college, null);
			paidVo.setTotal((listTotal != null) ? listTotal.size() : 0);
			totalNum += (listTotal != null) ? listTotal.size() : 0;
			listPaidVo.add(paidVo);
		}

		model.addAttribute("listPaidVo", listPaidVo);
		model.addAttribute("totalNum", totalNum);
		model.addAttribute("paidNum", paidNum);
		model.addAttribute("unpaidNum", unpaidNum);
		model.addAttribute("greenNum", greenNum);
		return Constants.REGULATORY_ANALYSIS + "/payment";
	}

	/**
	 * 分时办理统计
	 * 
	 * @param model
	 * @param request
	 * @param startDate
	 * @param endDate
	 * @param apartMinute
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = { Constants.TIME_COUNT + "/opt-query/timeCount" })
	public String timeCount(ModelMap model, HttpServletRequest request,
			String startDate, String endDate, Integer apartMinute)
			throws Exception {
		log.info("分时办理统计");
		// 添加学院条件
		String defaultCollegeId = ProjectSessionUtils
				.getCurrentTeacherOrgId(request);
		if (!CheckUtils.isCurrentOrgEqCollege(defaultCollegeId)) {
			defaultCollegeId = null;
		}
		List<TimeCountVo> list = regulatoryAnalysisService
				.countApartTimeReport(startDate, endDate, apartMinute,
						defaultCollegeId);
		List<Dic> listSite = dicUtil.getDicInfoList("REPORT_SITE");

		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("apartMinute", apartMinute);
		model.addAttribute("list", list);
		model.addAttribute("listSite", listSite);
		return Constants.REGULATORY_ANALYSIS + "/timeCount";
	}

	/**
	 * 实时报到统计
	 * 
	 * @param model
	 * @param request
	 * @param range
	 * @return
	 */
	@RequestMapping(value = { Constants.LIVE_COUNT + "/opt-query/liveCount" })
	public String liveCount(ModelMap model, HttpServletRequest request,
			String range) {
		log.info("实时办理统计");
		Dic yearDic = currentYearDic();

		// List<StudentInfoModel> listT =
		// regulatoryAnalysisService.getStudentInfoModelByYear(currentYearDic(),
		// null);

		Long total = regulatoryAnalysisService.getStudentInfoCountByYear(
				currentYearDic(), null);
		// 录取总人数
		model.addAttribute("total", total);
		model.addAttribute("range", range);
		if (range != null && range.equals("2")) {
			// 学院
			List<BaseAcademyModel> collegeList = baseDataService
					.listBaseAcademy();
			List<CollegeRegistedVo> collegeRegistedList = new ArrayList<CollegeRegistedVo>();
			for (BaseAcademyModel college : collegeList) {
				CollegeRegistedVo vo = new CollegeRegistedVo();
				List<StudentInfoModel> listC = regulatoryAnalysisService
						.queryStudentInfoRegistedByCollege(yearDic, college,
								"registed");
				List<StudentInfoModel> listTC = regulatoryAnalysisService
						.queryStudentInfoRegistedByCollege(yearDic, college,
								null);
				vo.setCollege(college);
				vo.setRegistedNum((listC != null) ? listC.size() : 0);
				vo.setTotalNum((listTC != null) ? listTC.size() : 0);
				collegeRegistedList.add(vo);
			}
			model.addAttribute("collegeRegistedList", collegeRegistedList);
			return Constants.REGULATORY_ANALYSIS + "/liveCountCollege";
		} else {
			Integer countReport = regulatoryAnalysisService
					.queryStudentInfoModelCurrent(yearDic);
			model.addAttribute("countReport", countReport);
			return Constants.REGULATORY_ANALYSIS + "/liveCount";
		}
	}

	@ResponseBody
	@RequestMapping(value = { Constants.LIVE_COUNT + "/nsm/liveCount" })
	public String liveCountMsn(ModelMap model, HttpServletRequest request) {
		log.info("实时办理统计");
		Dic yearDic = currentYearDic();
		Integer countReport = regulatoryAnalysisService
				.queryStudentInfoModelCurrent(yearDic);
		model.addAttribute("countReport", countReport);
		return "success";
	}

	/**
	 * 实时办理统计全校
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { Constants.LIVE_COUNT + "/nsm/liveCountView" })
	public String liveCountLoad(ModelMap model, HttpServletRequest request) {
		log.info("实时办理统计");
		Dic yearDic = currentYearDic();
		Integer countReport = regulatoryAnalysisService
				.queryStudentInfoModelCurrent(yearDic);
		model.addAttribute("countReport", countReport);
		return Constants.REGULATORY_ANALYSIS + "/liveCountView";
	}

	/**
	 * 按学院实时办理统计
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { Constants.LIVE_COUNT
			+ "/nsm/liveCountCollegeView" })
	public String liveCountCollegeLoad(ModelMap model,
			HttpServletRequest request) {
		log.info("按学院实时办理统计");
		Dic yearDic = currentYearDic();
		// 学院
		List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		List<CollegeRegistedVo> collegeRegistedList = new ArrayList<CollegeRegistedVo>();
		for (BaseAcademyModel college : collegeList) {
			CollegeRegistedVo vo = new CollegeRegistedVo();
			List<StudentInfoModel> listC = regulatoryAnalysisService
					.queryStudentInfoRegistedByCollege(yearDic, college,
							"registed");
			// List<StudentInfoModel> listT =
			// regulatoryAnalysisService.queryStudentInfoRegistedByCollege(yearDic,
			// college, null);
			vo.setCollege(college);
			vo.setRegistedNum((listC != null) ? listC.size() : 0);
			// vo.setTotalNum((listT!=null)?listT.size():0);
			collegeRegistedList.add(vo);
		}
		model.addAttribute("collegeRegistedList", collegeRegistedList);
		return Constants.REGULATORY_ANALYSIS + "/liveCountCollegeView";
	}

	/**
	 * 获取当前学年的数据字典
	 * 
	 * @return Dic
	 */
	private Dic currentYearDic() {
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		// SimpleDateFormat sdfm = new SimpleDateFormat("MM");
		// Date date = new Date();
		// yearDic = dicUtil.getDicInfo("YEAR",
		// sdf.format(date)+"_"+String.valueOf(Integer.parseInt(sdf.format(date))+1));
		// Dic yearDic = dicUtil.getDicInfo("YEAR", sdf.format(date));
		return SchoolYearUtil.getYearDic();
	}

	/**
	 * 根据班级获得 List<ReportStatisticsVo>
	 * 
	 * @param listKlass
	 * @return List<ReportStatisticsVo>
	 */
	private List<ReportStatisticsVo> getReportStatisticsVoByClass(
			List<BaseClassModel> listKlass) {
		if (listKlass != null && listKlass.size() > 0) {
			List<ReportStatisticsVo> listReportStatisticsVo = new ArrayList<ReportStatisticsVo>();
			// 添加要统计的省份
			List<Dic> listProvince = getCountProvince();

			for (BaseClassModel klass : listKlass) {
				ReportStatisticsVo reportStatisticsVo = new ReportStatisticsVo();
				// 存储班级
				reportStatisticsVo.setKlass(klass);
				List<ReportVo> listreportVo = new ArrayList<ReportVo>();
				// 录取人数
				Long reportStatisticsVoReportedNum = 0L;
				// 报到人数
				Long reportStatisticsVoRegistedNum = 0L;
				for (Dic province : listProvince) {
					ReportVo reportVo = new ReportVo();
					// 报到查询
					List<StudentInfoModel> listReported = regulatoryAnalysisService
							.queryStudentInfoByClassId(klass.getId(), province,
									"reported");
					// 录取查询
					List<StudentInfoModel> list = regulatoryAnalysisService
							.queryStudentInfoByClassId(klass.getId(), province,
									null);

					reportVo.setReportedNum((listReported != null) ? listReported
							.size() : 0L);
					reportVo.setTotalNum((list != null) ? list.size() : 0L);

					reportStatisticsVoReportedNum += ((list != null) ? list
							.size() : 0);
					reportStatisticsVoRegistedNum += ((listReported != null) ? listReported
							.size() : 0);

					listreportVo.add(reportVo);
				}
				// 录取人数添加到对象
				reportStatisticsVo
						.setReportedNum(reportStatisticsVoReportedNum);
				// 报到人数添加到对象
				reportStatisticsVo
						.setRegistedNum(reportStatisticsVoRegistedNum);

				reportStatisticsVo.setListReport(listreportVo);
				if (reportStatisticsVoReportedNum.longValue() > 0L)
					listReportStatisticsVo.add(reportStatisticsVo);
			}
			return listReportStatisticsVo;
		} else
			return null;
	}

	/**
	 * 返回一个字符串类型的两位小数的百分数
	 * 
	 * @param numerator
	 *            分子
	 * @param denominator
	 *            分母
	 * @return
	 */
	private String percentFormat(Long numerator, Long denominator) {
		// 学院报到率
		String rate = "0";
		if (denominator != null && denominator != 0) {
			NumberFormat numberFormat = NumberFormat.getInstance();
			numberFormat.setMaximumFractionDigits(2);
			rate = numberFormat.format((float) numerator / (float) denominator
					* 100)
					+ "%";
		} else {
			rate = "0%";
		}
		return rate;
	}

	/**
	 * 省份Dic获取方法
	 * 
	 * @return
	 */
	public List<Dic> getCountProvince() {
		// 添加要统计的省份
		List<Dic> listProvince = new ArrayList<Dic>();
		// 1.浙江
		listProvince.add(dicUtil.getDicInfo("NATIVE", "330000"));
		// 2.安徽
		listProvince.add(dicUtil.getDicInfo("NATIVE", "340000"));
		// 3.福建
		listProvince.add(dicUtil.getDicInfo("NATIVE", "350000"));
		// 4.贵州
		listProvince.add(dicUtil.getDicInfo("NATIVE", "520000"));
		// 5.河南
		listProvince.add(dicUtil.getDicInfo("NATIVE", "410000"));
		// 6.江苏
		listProvince.add(dicUtil.getDicInfo("NATIVE", "320000"));
		// 7.江西
		listProvince.add(dicUtil.getDicInfo("NATIVE", "360000"));
		// 8.山西
		listProvince.add(dicUtil.getDicInfo("NATIVE", "140000"));
		// 9.湖北
		listProvince.add(dicUtil.getDicInfo("NATIVE", "420000"));
		// 10.重庆
		listProvince.add(dicUtil.getDicInfo("NATIVE", "500000"));
		// 11.四川省
		listProvince.add(dicUtil.getDicInfo("NATIVE", "510000"));
		// 12.广西壮族自治区
		listProvince.add(dicUtil.getDicInfo("NATIVE", "450000"));
		// 13.新疆维吾尔自治区
		listProvince.add(dicUtil.getDicInfo("NATIVE", "650000"));
		// 14.广东省
		listProvince.add(dicUtil.getDicInfo("NATIVE", "440000"));
		// 15.甘肃省
		listProvince.add(dicUtil.getDicInfo("NATIVE", "620000"));
		// 16.宁夏回族自治区
		listProvince.add(dicUtil.getDicInfo("NATIVE", "640000"));
		// 17.其他省份
		listProvince.add(null);
		return listProvince;
	}

	/**
	 * 报到进度调用poi统一导出方法
	 * 
	 * @param response
	 * @param title
	 * @param xlsName
	 * @param voName
	 * @param listMap
	 */
	private void exportReportExcel(HttpServletResponse response, String title,
			String xlsName, String voName, List<Map> listMap) {
		try {
			HSSFWorkbook wb = excelService.exportData(xlsName, voName, listMap);
			String filename = (title) + "报到进度统计表" + ".xls";
			response.setContentType("application/x-excel");
			response.setHeader("Content-disposition", "attachment;filename="
					+ new String(filename.getBytes("GBK"), "iso-8859-1"));
			response.setCharacterEncoding("UTF-8");
			OutputStream ouputStream = response.getOutputStream();
			wb.write(ouputStream);
			ouputStream.flush();
			ouputStream.close();
		} catch (ExcelException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
