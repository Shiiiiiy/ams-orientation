package com.uws.orientation.service;

import java.text.ParseException;
import java.util.List;

import com.uws.core.base.IBaseService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseMajorModel;
import com.uws.domain.orientation.CountExReportVo;
import com.uws.domain.orientation.KlassSourceLandVo;
import com.uws.domain.orientation.ReportProgressVo;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.orientation.StudentReportModel;
import com.uws.domain.orientation.TimeCountVo;
import com.uws.sys.model.Dic;

public interface IRegulatoryAnalysisService extends IBaseService {

	/**
	 * 预期报到查询
	 * 
	 * @param dEFAULT_PAGE_SIZE
	 * @param pageNo
	 * @param studentReportModel
	 * @return Page
	 */
	public Page queryPageExReport(int pageSize, Integer pageNo,
			StudentReportModel studentReportModel);

	/**
	 * 预期报到统计，时间段、间隔时间统计
	 * 
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @param apartHour
	 *            间隔时间
	 * @throws ParseException
	 */
	public List<CountExReportVo> countExReport(String startDate,
			String endDate, Integer apartMinute, String collegeId)
			throws ParseException;

	/**
	 * 查询StudentReportModel的所有对象
	 * 
	 * @return
	 */
	public List<StudentReportModel> getAllStudentReportModel(Dic yearDic);

	/**
	 * 报到点统计
	 * 
	 * @param yearDic
	 * @param reportSite
	 * @return
	 */
	public List<StudentInfoModel> getAllStudentInfo(Dic yearDic, Dic reportSite);

	/**
	 * 查询绿色通道数据
	 * 
	 * @param pageSize
	 * @param pageNo
	 * @param reportProgressVo
	 * @param flag
	 *            值为green查询全部数据
	 * @return Page
	 */
	public Page queryPageGreenChannel(int pageSize, Integer pageNo,
			ReportProgressVo reportProgressVo, String flag);

	/**
	 * 按照ReportProgressVo查询StudentReportModel，若flag="reported"返回已报名情况
	 * 
	 * @param reportProgressVo
	 * @param flag
	 * @return List<StudentReportModel>
	 */
	public List<StudentInfoModel> queryStudentInfoByClassId(String klassId,
			Dic province, String flag);

	/**
	 * 根据学年查、学院询缴费信息
	 * 
	 * @param currentYearDic
	 * @param college
	 * @return List<StudentReportModel>
	 */
	public List<StudentInfoModel> queryCostState(Dic currentYearDic,
			BaseAcademyModel college, String flag);

	/**
	 * 分时统计各报到点报到情况
	 * 
	 * @param startDate
	 * @param endDate
	 * @return List<TimeCountVo>
	 */
	public List<TimeCountVo> countApartTimeReport(String startDate,
			String endDate, Integer apartMinute, String collegeId)
			throws Exception;

	/**
	 * 实时统计各报到点报到情况
	 * 
	 * @return
	 */
	public Integer queryStudentInfoModelCurrent(Dic yearDic);

	/**
	 * 通过学年查找StudentReportModel
	 * 
	 * @param Dic
	 *            year
	 * @return
	 */
	public List<StudentReportModel> getStudentReportModelByYear(Dic year,
			String flag);

	/**
	 * 通过学年，学院，省份查询
	 * 
	 * @param yearDic
	 * @param college
	 * @param province
	 * @param flag
	 * @return List<StudentInfoModel>
	 */
	public List<StudentInfoModel> queryStudentInfoByCollege(String yearId,
			BaseAcademyModel college, Dic province, String flag);

	public Long queryStudentInfoCountByCollege(String yearId,
			BaseAcademyModel college, Dic province, String flag);

	/**
	 * 通过学年，专业，省份查询
	 * 
	 * @param id
	 * @param major
	 * @param province
	 * @param flag
	 * @return List<StudentInfoModel>
	 */
	public List<StudentInfoModel> queryStudentInfoByMajor(String yearId,
			BaseMajorModel major, Dic province, String flag);

	public Long queryStudentInfoCountByMajor(String yearId,
			BaseMajorModel major, Dic province, String flag);

	/**
	 * 通过学年，班级，省份查询
	 * 
	 * @param id
	 * @param klass
	 * @param p
	 * @param string
	 * @return List<StudentInfoModel>
	 */
	public List<StudentInfoModel> queryStudentInfoByKlass(String yearId,
			BaseClassModel klass, Dic province, String flag);

	public Long queryStudentInfoCountByKlass(String yearId,
			BaseClassModel klass, Dic province, String flag);

	public List<KlassSourceLandVo> queryStudentInfoLPByKlass(String yearId,
			BaseClassModel klass, String flag);

	/**
	 * 通过入学年份查询报到状态
	 * 
	 * @param yearDic
	 * @param flag
	 * @return
	 */
	public List<StudentInfoModel> getStudentInfoModelByYear(Dic yearDic,
			String flag);

	/**
	 * 根据当前学年，学院查询报到人数
	 * 
	 * @param yearDic
	 * @param college
	 * @param flag
	 *            有标记的为报到 null值为录取人数
	 * @return
	 */
	public List<StudentInfoModel> queryStudentInfoRegistedByCollege(
			Dic yearDic, BaseAcademyModel college, String flag);

	public Long getStudentInfoCountByYear(Dic yearDic, String flag);

	/**
	 * 通过入学年份查询所有省份
	 * 
	 * @param yearDic
	 * @return
	 */
	public List<String> getProvinceDicByEnterYear(Dic yearDic);

	/**
	 * 
	 * @param yearId
	 * @param type
	 * @param objectId
	 * @return
	 */
	public String getStudentReportProcedureResult(String yearId, String type,
			String objectId);

	/**
	 * 
	 * @param yearId
	 * @param range
	 * @param collegeId
	 * @param majorId
	 * @param klassId
	 * @return List<ReportProgressCountVo>
	 */
	@Deprecated
	public List<Object[]> getReportProgressCountVo(String yearId, String range,
			List<String> lp, String collegeId, String majorId, String klassId);

	/**
	 * 
	 * @param yearId
	 * @param ls
	 * @param collegeId
	 * @param majorId
	 * @param klassId
	 * @param flag
	 *            如果为null则为全校情况
	 * @return
	 */
	List<Object[]> getOtherProvinceCount(String yearId, String collegeId,
			String majorId, String klassId, String flag);

	/**
	 * 查询预报道导出列表
	 * 
	 * @Description: TODO
	 * @author: 唐靖
	 * @date: 2016-11-21 下午2:25:12
	 * @param exportSize
	 * @param exportPage
	 * @param studentReportModel
	 * @return
	 */
	public Page countExReportList(String exportSize, String exportPage,
			StudentReportModel studentReportModel);

	/**
	 * 根据生源地省份查询报表数据
	 * 
	 * @Description: TODO
	 * @author: 唐靖
	 * @date: 2016-11-22 下午1:49:58
	 * @param id
	 * @param range
	 * @param provinceDicList
	 * @param defaultCollegeId
	 * @param majorId
	 * @param klassId
	 * @return
	 */
	public List<Object[]> getReportProgressCountVoByProDic(String id,
			String range, List<Dic> provinceDicList, String defaultCollegeId,
			String majorId, String klassId);
}
