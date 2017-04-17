package com.uws.orientation.dao;

import java.util.Date;
import java.util.List;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseMajorModel;
import com.uws.domain.orientation.KlassSourceLandVo;
import com.uws.domain.orientation.ReportProgressVo;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.orientation.StudentReportModel;
import com.uws.sys.model.Dic;

public interface IRegulatoryAnalysisDao extends IBaseDao {

	/**
	 * Page
	 * 
	 * @param dEFAULT_PAGE_SIZE
	 * @param pageNo
	 * @param studentReportModel
	 * @return Page
	 */
	public Page queryPageExReport(int pageSize, Integer pageNo,
			StudentReportModel studentReportModel);

	/**
	 * 按照开始时间 结束时间来查询预期报到
	 * 
	 * @param startDate
	 *            开始时间 年月日时分
	 * @param endDate
	 *            结束时间 年月日时分
	 * @param collegeId
	 *            判断归属学院
	 * @return
	 */
	public List<StudentReportModel> queryExReport(Date startDate, Date endDate,
			String collegeId);

	/**
	 * 按照开始时间 结束时间来查询学生报到情况
	 * 
	 * @param pointDate
	 * @param finalDate
	 * @return List<StudentInfoModel>
	 */
	public List<StudentInfoModel> queryReported(Date startDate, Date endDate,
			String collegeId);

	/**
	 * 获得StudentReportModel类的所有对象的方法
	 * 
	 * @return List<StudentReportModel>
	 */
	public List<StudentInfoModel> getAllStudentInfo(Dic yearDic,
			Dic reportSite, String flag);

	/**
	 * 查询绿色通道数据
	 * 
	 * @param pageSize
	 * @param pageNo
	 * @param reportProgressVo
	 * @return
	 */
	public Page queryPageGreenChannel(int pageSize, Integer pageNo,
			ReportProgressVo reportProgressVo, String flag);

	/**
	 * 通过reportProgressVo类查询StudentReportModel方法
	 * 
	 * @param reportProgressVo
	 * @return List<StudentReportModel>
	 */
	public List<StudentInfoModel> queryStudentInfoByClassId(String klassId,
			Dic province, String flag);

	/**
	 * 通过学年、学院查询缴费信息
	 * 
	 * @param currentYearDic
	 * @param college
	 * @return List<StudentReportModel>
	 */
	public List<StudentInfoModel> queryCostState(Dic currentYearDic,
			BaseAcademyModel college, String flag);

	/**
	 * 通过开始时间，结束时间，报名地点查询StudentReportModel
	 * 
	 * @param pointDate
	 * @param finalDate
	 * @param place
	 * @return
	 */
	public List<StudentInfoModel> getStudentInfoModelByPlace(Date pointDate,
			Date finalDate, Dic place);

	/**
	 * 通过学年查找StudentReportModel
	 * 
	 * @param year
	 * @return
	 */
	public List<StudentReportModel> getStudentReportModelByYear(Dic year,
			String flag);

	/**
	 * 获得预期报到数据
	 * 
	 * @param yearDic
	 * @param reportSite
	 * @return
	 */
	public List<StudentReportModel> getAllStudentReportModel(Dic yearDic);

	/**
	 * 通过学年，学院，省份查询
	 * 
	 * @param yearDic
	 * @param college
	 * @param province
	 * @return List<StudentInfoModel>
	 */
	public List<StudentInfoModel> queryStudentInfoByCollege(String yearId,
			BaseAcademyModel college, Dic province, String flag);

	public Long queryStudentInfoCountByCollege(String yearId,
			BaseAcademyModel college, Dic province, String flag);

	/**
	 * 通过学年，专业，省份查询
	 * 
	 * @param yearId
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
	 * 
	 * @param yearId
	 * @param klass
	 * @param province
	 * @param flag
	 * @return List<StudentInfoModel>
	 */
	public List<StudentInfoModel> queryStudentInfoByKlass(String yearId,
			BaseClassModel klass, Dic province, String flag);

	public Long queryStudentInfoCountByKlass(String yearId,
			BaseClassModel klass, Dic province, String flag);

	/**
	 * 
	 * @param yearDic
	 * @param flag
	 * @return
	 */
	public List<StudentInfoModel> getStudentInfoModelByYear(Dic yearDic,
			String flag);

	public Long getStudentInfoCountByYear(Dic yearDic, String flag);

	public List<KlassSourceLandVo> queryStudentInfoLPByKlass(String yearId,
			BaseClassModel klass, String flag);

	/**
	 * 
	 * @param startDate
	 * @param finalDate
	 * @return
	 */
	List<StudentInfoModel> getStudentInfoModelByTime(Date startDate,
			Date finalDate, String collegeId);

	/**
	 * 
	 * @param yearDic
	 * @param college
	 * @param flag
	 * @return
	 */
	public List<StudentInfoModel> queryStudentInfoRegistedByCollege(
			Dic yearDic, BaseAcademyModel college, String flag);

	/**
	 * 
	 * @param yearDic
	 * @return List<Dic>
	 */
	public List<String> getProvinceDicByEnterYear(String yearId);

	/**
	 * 报到进度统计方法
	 * 
	 * @param yearId
	 *            学年
	 * @param lp
	 *            省份code List
	 * @param range
	 *            按什么方式查询
	 * @param collegeId
	 *            学院id
	 * @param majorId
	 *            专业id
	 * @param klassId
	 *            班级id
	 * @return
	 */
	@Deprecated
	public List<Object[]> getReportProgress(String yearId, List<String> lp,
			String range, String collegeId, String majorId, String klassId);

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

	public Page queryExReportList(String exportSize, String exportPage,
			StudentReportModel studentReportModel);

	/**
	 * 根据生源地字典查询学生结果
	 * 
	 * @Description: TODO
	 * @author: 唐靖
	 * @date: 2016-11-22 下午1:52:21
	 * @param yearId
	 * @param lp
	 * @param range
	 * @param collegeId
	 * @param majorId
	 * @param klassId
	 * @return
	 */
	public List<Object[]> getReportProgressByProDic(String yearId,
			List<Dic> lp, String range, String collegeId, String majorId,
			String klassId);
}
