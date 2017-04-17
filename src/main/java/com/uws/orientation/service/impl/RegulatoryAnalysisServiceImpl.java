package com.uws.orientation.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uws.core.base.BaseServiceImpl;
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
import com.uws.orientation.dao.IRegulatoryAnalysisDao;
import com.uws.orientation.service.IRegulatoryAnalysisService;
import com.uws.sys.model.Dic;
import com.uws.sys.service.DicUtil;
import com.uws.task.dao.IExecuProcedureDao;

@Service("com.uws.orientation.service.impl.RegulatoryAnalysisServiceImpl")
public class RegulatoryAnalysisServiceImpl extends BaseServiceImpl implements
		IRegulatoryAnalysisService {

	@Autowired
	IRegulatoryAnalysisDao regulatoryAnalysisDao;

	@Autowired
	DicUtil dicUtil;

	@Autowired
	IExecuProcedureDao execuProcedureDao;

	@Override
	public Page queryPageExReport(int pageSize, Integer pageNo,
			StudentReportModel studentReportModel) {
		return regulatoryAnalysisDao.queryPageExReport(Page.DEFAULT_PAGE_SIZE,
				pageNo, studentReportModel);
	}

	@Override
	public List<CountExReportVo> countExReport(String startDate,
			String endDate, Integer apartMinute/* 分钟 */, String collegeId)
			throws ParseException {
		if (startDate != null && endDate != null && apartMinute != null) {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date startDateText = sdf.parse(startDate);
			Date endDateText = sdf.parse(endDate);

			Calendar cal = Calendar.getInstance();

			Date pointDate = startDateText;
			List<CountExReportVo> listCountExReport = new ArrayList<CountExReportVo>();

			// 时间列表
			while (pointDate.compareTo(endDateText) < 0) {
				cal.setTime(pointDate);
				cal.add(Calendar.MINUTE, apartMinute);
				// 结束时间
				Date finalDate = cal.getTime();
				// 假如叠加时间查过结束时间，则截止时间设置为结束时间
				if (cal.getTime().compareTo(endDateText) > 0)
					finalDate = endDateText;

				Integer carNum = 0;
				Integer bedNum = 0;
				Integer moveNum = 0;
				Integer peopleTogether = 0;
				// 查询符合时间段的预期报到表
				List<StudentReportModel> list = regulatoryAnalysisDao
						.queryExReport(pointDate, finalDate, collegeId);
				if (list != null && list.size() > 0) {
					for (StudentReportModel studentReportModel : list) {
						// 自备车
						if (studentReportModel.getIsCar() != null
								&& studentReportModel.getIsCar().equals("1")) {
							carNum++;
						}
						// 寝具包
						if (studentReportModel.getBed() != null
								&& studentReportModel.getBed().equals("1")) {
							bedNum++;
						}
						// 迁户口
						if (studentReportModel.getMove() != null
								&& studentReportModel.getMove().equals("1")) {
							moveNum++;
						}
						// 随行人数
						if (studentReportModel.getTogether() != null
								&& studentReportModel.getTogether() != 0) {
							peopleTogether += studentReportModel.getTogether();
						}
					}
				}
				CountExReportVo vo = new CountExReportVo();
				// 行名称（时间段名称）
				vo.setName(sdf.format(pointDate) + " 至 "
						+ sdf.format(finalDate));
				// 新生人数
				vo.setNum((list != null && list.size() > 0) ? list.size() : 0);
				// 自备车人数
				vo.setCars(carNum);
				// 需要寝具包人数
				vo.setBeds(bedNum);
				// 需要迁户口人数
				vo.setMoves(moveNum);
				// 随行总人数
				vo.setTogethers(peopleTogether);

				listCountExReport.add(vo);
				// 开始日期向后移
				pointDate = finalDate;
			}
			return listCountExReport;
		}
		return null;

	}

	@Override
	public List<StudentReportModel> getAllStudentReportModel(Dic yearDic) {
		return regulatoryAnalysisDao.getAllStudentReportModel(yearDic);
	}

	@Override
	public List<StudentInfoModel> getAllStudentInfo(Dic yearDic, Dic reportSite) {
		return regulatoryAnalysisDao.getAllStudentInfo(yearDic, reportSite,
				null);
	}

	@Override
	public Page queryPageGreenChannel(int pageSize, Integer pageNo,
			ReportProgressVo reportProgressVo, String flag) {
		return regulatoryAnalysisDao.queryPageGreenChannel(pageSize, pageNo,
				reportProgressVo, flag);
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoByClassId(String klass,
			Dic province, String flag) {
		return regulatoryAnalysisDao.queryStudentInfoByClassId(klass, province,
				flag);
	}

	@Override
	public List<StudentInfoModel> queryCostState(Dic currentYearDic,
			BaseAcademyModel college, String flag) {
		return regulatoryAnalysisDao.queryCostState(currentYearDic, college,
				flag);
	}

	@Override
	public List<TimeCountVo> countApartTimeReport(String startDate,
			String endDate, Integer apartMinute, String collegeId)
			throws Exception {
		if (startDate != null && endDate != null && apartMinute != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date startDateText = sdf.parse(startDate);
			Date endDateText = sdf.parse(endDate);

			Calendar cal = Calendar.getInstance();

			Date pointDate = startDateText;
			List<TimeCountVo> listTimeCount = new ArrayList<TimeCountVo>();
			List<Dic> listPlace = dicUtil.getDicInfoList("REPORT_SITE");

			// 时间列表
			while (pointDate.compareTo(endDateText) < 0) {
				//
				TimeCountVo timeCount = new TimeCountVo();
				// List<PlaceCountVo> listPlaceCount = new
				// ArrayList<PlaceCountVo>();
				cal.setTime(pointDate);
				cal.add(Calendar.MINUTE, apartMinute);
				// 结束时间
				Date finalDate = cal.getTime();
				// 假如叠加时间查过结束时间，则截止时间设置为结束时间
				if (cal.getTime().compareTo(endDateText) > 0)
					finalDate = endDateText;

				timeCount.setTimeLine(sdf.format(pointDate) + " 至 "
						+ sdf.format(finalDate));

				// 查询符合时间段的报到地点的报到情况
				List<StudentInfoModel> list = regulatoryAnalysisDao
						.queryReported(pointDate, finalDate, collegeId);
				List<StudentInfoModel> listTotal = regulatoryAnalysisDao
						.getStudentInfoModelByTime(startDateText, endDateText,
								collegeId);
				timeCount.setReportNum(list != null ? list.size() : 0);
				timeCount.setTotalNum(listTotal != null ? listTotal.size() : 0);

				/**
				 * for(Dic place:listPlace) { //总时间跨度在本报名点的预报名数据
				 * List<StudentInfoModel> listTotal =
				 * regulatoryAnalysisDao.getStudentReportModelByPlace
				 * (startDateText, endDateText, place); //本时间段在本报名点的预报名数据
				 * List<StudentInfoModel> listSite =
				 * regulatoryAnalysisDao.getStudentReportModelByPlace(pointDate,
				 * finalDate, place); PlaceCountVo placeCount = new
				 * PlaceCountVo(); placeCount.setPlace(place);
				 * placeCount.setNum((listTotal!=null)?listTotal.size():0);
				 * placeCount.setReportNum((listSite!=null)?listSite.size():0);
				 * //添加到报名点统计列表中 listPlaceCount.add(placeCount); }
				 */
				// timeCount.setListPlaceCount(listPlaceCount);
				listTimeCount.add(timeCount);
				// 开始日期向后移
				pointDate = finalDate;
			}
			return listTimeCount;
		}
		return null;

	}

	/**
	 * @Override public List<PlaceCountVo> queryStudentReportModelCurrent(Dic
	 *           yearDic) { List<Dic> listPlace =
	 *           dicUtil.getDicInfoList("REPORT_SITE"); List<PlaceCountVo> list
	 *           = new ArrayList<PlaceCountVo>(); for(Dic place:listPlace) {
	 *           PlaceCountVo pc = new PlaceCountVo(); List<StudentInfoModel>
	 *           temp = regulatoryAnalysisDao.getAllStudentInfo(yearDic, place,
	 *           "current"); pc.setPlace(place);
	 *           pc.setNum((temp!=null)?temp.size():0);
	 * 
	 *           list.add(pc); } return (list!=null && list.size()>0)?list:null;
	 *           }
	 */

	@Override
	public Integer queryStudentInfoModelCurrent(Dic yearDic) {
		List<StudentInfoModel> temp = regulatoryAnalysisDao.getAllStudentInfo(
				yearDic, null, "current");
		return (temp != null) ? temp.size() : 0;
	}

	@Override
	public List<StudentReportModel> getStudentReportModelByYear(Dic year,
			String flag) {
		return regulatoryAnalysisDao.getStudentReportModelByYear(year, flag);
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoByCollege(String yearId,
			BaseAcademyModel college, Dic province, String flag) {
		return regulatoryAnalysisDao.queryStudentInfoByCollege(yearId, college,
				province, flag);
	}

	@Override
	public Long queryStudentInfoCountByCollege(String yearId,
			BaseAcademyModel college, Dic province, String flag) {
		return regulatoryAnalysisDao.queryStudentInfoCountByCollege(yearId,
				college, province, flag);
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoByMajor(String YearId,
			BaseMajorModel major, Dic province, String flag) {
		return regulatoryAnalysisDao.queryStudentInfoByMajor(YearId, major,
				province, flag);
	}

	@Override
	public Long queryStudentInfoCountByMajor(String YearId,
			BaseMajorModel major, Dic province, String flag) {
		return regulatoryAnalysisDao.queryStudentInfoCountByMajor(YearId,
				major, province, flag);
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoByKlass(String yearId,
			BaseClassModel klass, Dic province, String flag) {
		return regulatoryAnalysisDao.queryStudentInfoByKlass(yearId, klass,
				province, flag);
	}

	@Override
	public Long queryStudentInfoCountByKlass(String yearId,
			BaseClassModel klass, Dic province, String flag) {
		return regulatoryAnalysisDao.queryStudentInfoCountByKlass(yearId,
				klass, province, flag);
	}

	@Override
	public List<KlassSourceLandVo> queryStudentInfoLPByKlass(String yearId,
			BaseClassModel klass, String flag) {
		return regulatoryAnalysisDao.queryStudentInfoLPByKlass(yearId, klass,
				flag);
	}

	@Override
	public List<StudentInfoModel> getStudentInfoModelByYear(Dic yearDic,
			String flag) {
		return regulatoryAnalysisDao.getStudentInfoModelByYear(yearDic, flag);
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoRegistedByCollege(
			Dic yearDic, BaseAcademyModel college, String flag) {
		return regulatoryAnalysisDao.queryStudentInfoRegistedByCollege(yearDic,
				college, flag);
	}

	public Long getStudentInfoCountByYear(Dic yearDic, String flag) {
		return regulatoryAnalysisDao.getStudentInfoCountByYear(yearDic, flag);
	}

	@Override
	public List<String> getProvinceDicByEnterYear(Dic yearDic) {
		return regulatoryAnalysisDao.getProvinceDicByEnterYear(yearDic.getId());
	}

	@Override
	public String getStudentReportProcedureResult(String yearId, String type,
			String objectId) {
		return execuProcedureDao.getStudentReportProcedureResult(yearId, type,
				objectId);
	}

	@Override
	@Deprecated
	public List<Object[]> getReportProgressCountVo(String yearId, String range,
			List<String> lp, String collegeId, String majorId, String klassId) {
		return regulatoryAnalysisDao.getReportProgress(yearId, lp, range,
				collegeId, majorId, klassId);
	}

	@Override
	public List<Object[]> getOtherProvinceCount(String yearId,
			String collegeId, String majorId, String klassId, String flag) {
		return regulatoryAnalysisDao.getOtherProvinceCount(yearId, collegeId,
				majorId, klassId, flag);
	}

	/**
	 * 查询预报道导出列表
	 * 
	 * @Description: TODO
	 * @author: 唐靖
	 * @date: 2016-11-21 下午2:25:49
	 * @param exportSize
	 * @param exportPage
	 * @param studentReportModel
	 * @return
	 */
	@Override
	public Page countExReportList(String exportSize, String exportPage,
			StudentReportModel studentReportModel) {

		return regulatoryAnalysisDao.queryExReportList(exportSize, exportPage,
				studentReportModel);
	}

	@Override
	public List<Object[]> getReportProgressCountVoByProDic(String yearId,
			String range, List<Dic> lp, String collegeId, String majorId,
			String klassId) {
		return regulatoryAnalysisDao.getReportProgressByProDic(yearId, lp,
				range, collegeId, majorId, klassId);
	}
}
