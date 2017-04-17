package com.uws.orientation.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uws.common.util.SchoolYearUtil;
import com.uws.core.base.BaseController;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.DataUtil;
import com.uws.core.util.StringUtils;
import com.uws.domain.orientation.WelcomeSetModel;
import com.uws.log.Logger;
import com.uws.log.LoggerFactory;
import com.uws.orientation.service.IStudentInfoService;
import com.uws.orientation.service.IWelcomeSetService;
import com.uws.orientation.util.Constants;
import com.uws.sys.model.Dic;
import com.uws.user.model.User;

/**
 * 
 * @ClassName: WelcomSetController
 * @Description: TODO(迎新数据设置的Controller功能描述：对迎新数据的新增、修改、查询等操作)
 * @author wangcl
 * @date 2015-7-24 上午9:46:48
 * 
 */
@Controller
public class WelcomeSetController extends BaseController {

	// 日志
	private Logger logger = new LoggerFactory(
			NewStudentRegisterController.class);
	
	// session的共通操作
	private SessionUtil sessionUtil = SessionFactory
				.getSession(Constants.WELCOME_SET);

	// 迎新数据设置信息的Service
	@Autowired
	private IWelcomeSetService welcomeSetService;
	
	// 学生信息的Service
	@Autowired
	private IStudentInfoService studentInfoService;
	
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

	/**
	 * 进入迎新数据设置页面
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { Constants.WELCOME_SET
			+ "/opt-update/editWelcomeSet" })
	public String editWelcomeSet(ModelMap model, HttpServletRequest request) {
		//日志
		logger.info("进入迎新数据设置页面");
		// 取得迎新设置信息
		WelcomeSetModel wsm = welcomeSetService.getWelcomeSet();
		if (wsm == null) {
			wsm = new WelcomeSetModel();
		}
		
		//迎新设置信息
		model.addAttribute("welcomeSet", wsm);

		// 初始完成页面跳转到社会实践活动的编辑页面。
		return Constants.WELCOME_SET_FTL + "editDataSet";
	}
	
	/**
	 * 添加，修改迎新数据设置的提交
	 * 
	 * @param WelcomeSetModel
	 *            迎新数据设置Model
	 * @return
	 */
	@RequestMapping(value = {
			Constants.WELCOME_SET + "/opt-add/submitWelcomeSet"})
	public String submitWelcomeSet(@Valid WelcomeSetModel welcomeSet,ModelMap model) {
		// 判断是否有Id 有：则是修改 ，没有：则是添加
		if (StringUtils.hasText(welcomeSet.getId())) {
			logger.info("迎新数据设置修改的处理");
			
			//修改人
			welcomeSet.setUpdater(new User(sessionUtil.getCurrentUserId()));
			//修改时间
			welcomeSet.setUpdateTime(new Date());
			
			// 进行更新
			welcomeSetService.updateWelcomeSet(welcomeSet);
		} else {
			logger.info("迎新数据设置新增的处理");
			// 进行新增操作处理
			String id = DataUtil.createOID();
			// 设置Id
			welcomeSet.setId(id);
			//创建人
			welcomeSet.setCreator(new User(sessionUtil.getCurrentUserId()));
			// 删除标识
			//examPlanMainPo.setIsDelete(Constants.DELETE_FLAG);
			//创建时间
			welcomeSet.setCreateTime(new Date());
			// 执行保存操作
			welcomeSetService.saveWelcomeSet(welcomeSet);
		}
		//迎新设置信息
		model.addAttribute("welcomeSet", welcomeSet);
		
		//进入页面的标志
		model.addAttribute("flag", "save");
		
		// 操作完成返回查询页面
		return Constants.WELCOME_SET_FTL + "editDataSet";
	}
	
	
	/**
	 * 批量处理财务数据中缴费状态
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value={Constants.WELCOME_SET_FTL + "/nsm/costTotal"})
	public String liveCountMsn(ModelMap model, HttpServletRequest request) {
		//log.info("实时办理统计");
		Dic yearDic = SchoolYearUtil.getYearDic();
		//Integer countReport = regulatoryAnalysisService.queryStudentInfoModelCurrent(yearDic);
		
		studentInfoService.updateAllCost(yearDic);
		//model.addAttribute("countReport", countReport);
		return "success";
	}


}
