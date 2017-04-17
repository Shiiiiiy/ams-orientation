package com.uws.orientation.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.util.DataUtil;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseMajorModel;
import com.uws.domain.orientation.KlassSourceLandVo;
import com.uws.domain.orientation.ReportProgressVo;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.orientation.StudentReportModel;
import com.uws.orientation.dao.IRegulatoryAnalysisDao;
import com.uws.orientation.util.Constants;
import com.uws.sys.model.Dic;
import com.uws.sys.service.impl.DicFactory;

@Repository("com.uws.orientation.dao.impl.RegulatoryAnalysisDaoImpl")
public class RegulatoryAnalysisDaoImpl extends BaseDaoImpl implements
		IRegulatoryAnalysisDao {
	@Override
	public Page queryPageExReport(int pageSize, Integer pageNo,
			StudentReportModel studentReportModel) {

		sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"update HKY_STUDENT_INFO SET CANDIDATE_PROVENCE = (select id from DIC WHERE CODE =(SUBSTR(CANDIDATE_NUM, 3, 2))) WHERE LENGTH(CANDIDATE_PROVENCE) != 32")
				.executeUpdate(); // 修改生源地字段

		sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"update HKY_STUDENT_INFO SET CANDIDATE_PROVENCE = '"
								+ DicFactory
										.getDicUtil()
										.getDicInfo("DIC_CANDIDATE_PROVENCE",
												"OTHER_NATIVE").getId()
								+ "' where CANDIDATE_PROVENCE  is null")
				.executeUpdate(); // 修改生源地字段

		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentReportModel t where 1=1 ");
		if (null != studentReportModel) {
			// 学年
			if (studentReportModel.getYearDic() != null
					&& studentReportModel.getYearDic().getId() != null
					&& !studentReportModel.getYearDic().getId().equals("")) {
				hql.append(" and t.yearDic.id = ? ");
				values.add(studentReportModel.getYearDic().getId());
			}
			// 获取学生基本信息实体类
			if (studentReportModel.getStudentInfo() != null) {

				StudentInfoModel studentInfo = studentReportModel
						.getStudentInfo();
				// 学院
				if (studentInfo.getCollege() != null
						&& studentInfo.getCollege().getId() != null
						&& !studentInfo.getCollege().getId().equals("")) {
					hql.append(" and  t.studentInfo.college.id = ? ");
					values.add(studentInfo.getCollege().getId());
				}
				// 专业
				if (studentInfo.getMajor() != null
						&& studentInfo.getMajor().getId() != null
						&& !studentInfo.getMajor().getId().equals("")) {
					hql.append(" and  t.studentInfo.major.id = ? ");
					values.add(studentInfo.getMajor().getId());
				}
				// 班级
				if (studentInfo.getClassId() != null
						&& studentInfo.getClassId().getId() != null
						&& !studentInfo.getClassId().getId().equals("")) {
					hql.append(" and  t.studentInfo.classId.id = ? ");
					values.add(studentInfo.getClassId().getId());
				}
				// 姓名（模糊查询）
				if (studentInfo.getName() != null
						&& !studentInfo.getName().trim().equals("")) {
					hql.append(" and  t.studentInfo.name like ? ");
					values.add("%" + studentInfo.getName().trim() + "%");
				}
				// 学号
				/**
				 * if(studentInfo.getStuNumber()!=null &&
				 * !studentInfo.getStuNumber().trim().equals("")) {
				 * hql.append(" and  t.studentInfo.stuNumber like ? ");
				 * values.add("%" + studentInfo.getStuNumber().trim() + "%"); }
				 */
				// 身份证号(证件号码)
				/**
				 * if(studentInfo.getCertificateCode()!=null &&
				 * !studentInfo.getCertificateCode().trim().equals("")) {
				 * hql.append(" and  t.studentInfo.certificateCode like ? ");
				 * values.add("%" + studentInfo.getCertificateCode().trim() +
				 * "%"); }
				 */
			}
			// 是否报到
			if (DataUtil.isNotNull(studentReportModel.getIsReport())) {
				hql.append(" and  t.isReport = ? ");
				values.add(studentReportModel.getIsReport());
			}

			// 开始时间
			if (studentReportModel.getReportStartDate() != null) {
				hql.append(" and  t.reportDate >= ? ");
				values.add(studentReportModel.getReportStartDate());
			}
			// 结束时间
			if (studentReportModel.getReportEndDate() != null) {
				hql.append(" and  t.reportDate <= ? ");
				values.add(studentReportModel.getReportEndDate());
			}
			// 乘车方式
			if (studentReportModel.getRideWayDic() != null
					&& studentReportModel.getRideWayDic().getId() != null
					&& !studentReportModel.getRideWayDic().getId().equals("")) {
				hql.append(" and  t.rideWayDic.id = ? ");
				values.add(studentReportModel.getRideWayDic().getId());
			}
			// 到达站点
			if (studentReportModel.getSiteDic() != null
					&& studentReportModel.getSiteDic().getId() != null
					&& !studentReportModel.getSiteDic().getId().equals("")) {
				hql.append(" and  t.siteDic.id = ? ");
				values.add(studentReportModel.getSiteDic().getId());
			}
			// 需要空调
			/**
			 * if (studentReportModel.getAirCond()!=null &&
			 * !studentReportModel.getAirCond().equals("")) {
			 * hql.append(" and  t.airCond = ? ");
			 * values.add(studentReportModel.getAirCond()); }
			 */
			// 寝具包
			if (studentReportModel.getBed() != null
					&& !studentReportModel.getBed().equals("")) {
				hql.append(" and  t.bed = ? ");
				values.add(studentReportModel.getBed());
			}
			// 迁户口
			if (studentReportModel.getMove() != null
					&& !studentReportModel.getMove().equals("")) {
				hql.append(" and  t.move = ? ");
				values.add(studentReportModel.getMove());
			}
		}
		// 排序条件
		hql.append(" order by updateTime desc ");

		if (values.size() == 0)
			return this.pagedQuery(hql.toString(), pageNo, pageSize);
		else
			return this.pagedQuery(hql.toString(), pageNo, pageSize,
					values.toArray());
	}

	@Override
	public List<StudentReportModel> queryExReport(Date startDate, Date endDate,
			String collegeId) {
		List<Object> listQ = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder(
				"from StudentReportModel t where 1=1 and t.reportDate >= ? and t.reportDate <= ?");
		listQ.add(startDate);
		listQ.add(endDate);
		if (DataUtil.isNotNull(collegeId)) {
			hql.append("and t.studentInfo.college.id = ? ");
			listQ.add(collegeId);
		}
		List<StudentReportModel> list = (List<StudentReportModel>) this.query(
				hql.toString(), listQ.toArray());
		if (list != null && list.size() > 0)
			return list;
		else
			return null;
	}

	@Override
	public List<StudentInfoModel> queryReported(Date startDate, Date endDate,
			String collegeId) {
		List<Object> listQ = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder(
				"from StudentInfoModel t where 1=1 and t.reportDate >= ? and t.reportDate <= ?");
		listQ.add(startDate);
		listQ.add(endDate);
		if (DataUtil.isNotNull(collegeId)) {
			hql.append("and t.college.id = ? ");
			listQ.add(collegeId);
		}
		List<StudentInfoModel> list = (List<StudentInfoModel>) this.query(
				hql.toString(), listQ.toArray());
		if (list != null && list.size() > 0)
			return list;
		else
			return null;
	}

	@Override
	public List<StudentReportModel> getAllStudentReportModel(Dic yearDic) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				"from StudentReportModel t where 1=1 ");

		// 查询预报到学年
		hql.append(" and t.yearDic.id= ? ");
		values.add(yearDic.getId());

		// 预期报道统计是否报到
		hql.append(" and t.isReport = ? ");
		values.add(Constants.REGULATORY_NEED);

		List<StudentReportModel> list = (List<StudentReportModel>) this.query(
				hql.toString(), values.toArray());

		if (list != null && list.size() > 0)
			return list;
		else
			return null;
	}

	@Override
	public List<StudentInfoModel> getAllStudentInfo(Dic yearDic,
			Dic reportSite, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				"from StudentInfoModel t where 1=1 ");
		// 通过学生入学年份获得学年
		hql.append(" and t.enterYearDic.id= ? ");
		values.add(yearDic.getId());

		if (flag != null && flag.equals("current")) {
		} else {
			if (reportSite != null && reportSite.getId() != null
					&& !reportSite.getId().equals("")) {
				// 查询报到点
				hql.append(" and t.reportSiteDic.id = ? ");
				values.add(reportSite.getId());
			}
		}
		// 报到状态的学生
		hql.append(" and t.status = ? ");
		values.add(Constants.REGULATORY_NEED);
		List<StudentInfoModel> list = (List<StudentInfoModel>) this.query(
				hql.toString(), values.toArray());

		if (list != null && list.size() > 0)
			return list;
		else
			return null;
	}

	@Override
	public Page queryPageGreenChannel(int pageSize, Integer pageNo,
			ReportProgressVo reportProgressVo, String flag) {

		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");
		if (null != reportProgressVo) {
			// 学年对应的学生入学年份
			if (reportProgressVo.getYearDic() != null
					&& reportProgressVo.getYearDic().getId() != null) {
				hql.append(" and  t.enterYearDic.id = ? ");
				values.add(reportProgressVo.getYearDic().getId());
			}
			// 学院
			if (reportProgressVo.getCollege() != null
					&& !reportProgressVo.getCollege().equals("")) {
				hql.append(" and  t.college.id = ? ");
				values.add(reportProgressVo.getCollege());
			}
			// 专业
			if (reportProgressVo.getMajorId() != null
					&& !reportProgressVo.getMajorId().equals("")) {
				hql.append(" and  t.major.id = ? ");
				values.add(reportProgressVo.getMajorId());
			}
			// 班级
			if (reportProgressVo.getKlassId() != null
					&& !reportProgressVo.getKlassId().equals("")) {
				hql.append(" and  t.classId.id = ? ");
				values.add(reportProgressVo.getKlassId());
			}
		}
		hql.append(" and  t.status = ? ");
		values.add(Constants.REGULATORY_NEED);

		// 查询绿色通道
		if (flag != null && flag.equals("green")) {
			hql.append(" and  t.greenWay = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		// 排序条件
		hql.append(" order by updateTime desc ");
		if (values.size() == 0)
			return this.pagedQuery(hql.toString(), pageNo, pageSize);
		else
			return this.pagedQuery(hql.toString(), pageNo, pageSize,
					values.toArray());
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoByClassId(String klassId,
			Dic province, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");

		hql.append(" and t.classId.id = ? ");
		values.add(klassId);

		if (province == null) {
			hql.append(" and  t.sourceLand is null ");
		} else {
			hql.append(" and  t.sourceLand = ? ");
			values.add(province.getCode());
		}

		if (flag != null && flag.equals("reported")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		// 排序条件
		hql.append(" order by t.college.id, t.major.id, t.classId.id ");
		List<StudentInfoModel> list = (List<StudentInfoModel>) query(
				hql.toString(), values.toArray());

		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public List<StudentInfoModel> queryCostState(Dic currentYearDic,
			BaseAcademyModel college, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(currentYearDic.getId());

		hql.append(" and  t.college.id = ? ");
		values.add(college.getId());

		// 已缴费人数
		if (flag != null && flag.equals("paid")) {
			hql.append(" and  t.costState = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		// 未缴费人数
		if (flag != null && flag.equals("unpaid")) {
			hql.append(" and  (t.costState = ? ");
			values.add(Constants.REGULATORY_NEED_NOT);

			hql.append(" or t.costState is null ) ");
		}
		// 绿色通道
		if (flag != null && flag.equals("green")) {
			// 根据刘中兵建议，这个地方和绿色通道模块人数保持一致（都是在已报到的基础上查询绿色通道）
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);

			hql.append(" and  t.greenWay = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		List<StudentInfoModel> list = (List<StudentInfoModel>) query(
				hql.toString(), values.toArray());
		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public List<StudentInfoModel> getStudentInfoModelByPlace(Date startDate,
			Date finalDate, Dic place) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.reportDate >= ? ");
		values.add(startDate);

		hql.append(" and t.reportDate <= ? ");
		values.add(finalDate);

		hql.append(" and t.reportSiteDic.id = ? ");
		values.add(place.getId());

		List<StudentInfoModel> list = (List<StudentInfoModel>) query(
				hql.toString(), values.toArray());
		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public List<StudentInfoModel> getStudentInfoModelByTime(Date startDate,
			Date finalDate, String collegeId) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");
		hql.append(" and  t.reportDate >= ? ");
		values.add(startDate);
		hql.append(" and t.reportDate <= ? ");
		values.add(finalDate);
		if (DataUtil.isNotNull(collegeId)) {
			hql.append(" and t.college.id = ? ");
			values.add(collegeId);
		}
		List<StudentInfoModel> list = (List<StudentInfoModel>) query(
				hql.toString(), values);
		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public List<StudentReportModel> getStudentReportModelByYear(Dic year,
			String flag) {

		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentReportModel t where 1=1 ");

		hql.append(" and  t.yearDic.id = ? ");
		values.add(year.getId());

		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.studentInfo.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		List<StudentReportModel> list = (List<StudentReportModel>) query(
				hql.toString(), values.toArray());
		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoByCollege(String yearId,
			BaseAcademyModel college, Dic province, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearId);

		hql.append(" and  t.college.id = ? ");
		values.add(college.getId());

		if (province == null) {
			hql.append(" and  t.sourceLand is null ");
		} else {
			hql.append(" and  t.sourceLand = ? ");
			values.add(province.getCode());
		}

		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}

		List<StudentInfoModel> list = (List<StudentInfoModel>) query(
				hql.toString(), values.toArray());
		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public Long queryStudentInfoCountByCollege(String yearId,
			BaseAcademyModel college, Dic province, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" select count(t.id) from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearId);

		hql.append(" and  t.college.id = ? ");
		values.add(college.getId());

		if (province == null) {
			hql.append(" and  t.sourceLand is null ");
		} else {
			hql.append(" and  t.sourceLand = ? ");
			values.add(province.getCode());
		}

		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		return (Long) queryUnique(hql.toString(), values.toArray());
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoByMajor(String yearId,
			BaseMajorModel major, Dic province, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearId);

		hql.append(" and  t.major.id = ? ");
		values.add(major.getId());
		if (province == null) {
			hql.append(" and  t.sourceLand is null ");
		} else {
			hql.append(" and  t.sourceLand = ? ");
			values.add(province.getCode());
		}
		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		List<StudentInfoModel> list = (List<StudentInfoModel>) query(
				hql.toString(), values.toArray());
		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public Long queryStudentInfoCountByMajor(String yearId,
			BaseMajorModel major, Dic province, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" select count(t.id) from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearId);

		hql.append(" and  t.major.id = ? ");
		values.add(major.getId());
		if (province == null) {
			hql.append(" and  t.sourceLand is null ");
		} else {
			hql.append(" and  t.sourceLand = ? ");
			values.add(province.getCode());
		}
		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		return (Long) queryUnique(hql.toString(), values.toArray());
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoByKlass(String yearId,
			BaseClassModel klass, Dic province, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearId);

		hql.append(" and  t.classId.id = ? ");
		values.add(klass.getId());

		if (province == null) {
			hql.append(" and  t.sourceLand is null ");
		} else {
			hql.append(" and  t.sourceLand = ? ");
			values.add(province.getCode());
		}

		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		List<StudentInfoModel> list = (List<StudentInfoModel>) query(
				hql.toString(), values.toArray());
		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public Long queryStudentInfoCountByKlass(String yearId,
			BaseClassModel klass, Dic province, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" select count(t.id) from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearId);

		hql.append(" and  t.classId.id = ? ");
		values.add(klass.getId());

		if (province == null) {
			hql.append(" and  t.sourceLand is null ");
		} else {
			hql.append(" and  t.sourceLand = ? ");
			values.add(province.getCode());
		}

		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		return (Long) queryUnique(hql.toString(), values.toArray());
	}

	@Override
	public List<KlassSourceLandVo> queryStudentInfoLPByKlass(String yearId,
			BaseClassModel klass, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" select new com.uws.domain.orientation.KlassSourceLandVo(count(t.sourceLand),"
						+ " t.classId.id, t.sourceLand) from StudentInfoModel t where 1=1 ");
		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearId);
		hql.append(" and  t.classId.id = ? ");
		values.add(klass.getId());
		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		hql.append(" group by t.sourceLand, t.classId ");
		return (List<KlassSourceLandVo>) query(hql.toString(), values.toArray());
	}

	@Override
	public List<StudentInfoModel> getStudentInfoModelByYear(Dic yearDic,
			String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");
		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearDic.getId());
		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		List<StudentInfoModel> list = (List<StudentInfoModel>) query(
				hql.toString(), values.toArray());
		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public Long getStudentInfoCountByYear(Dic yearDic, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" select count(t.id) from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearDic.getId());
		if (flag != null && flag.equals("registed")) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}

		return (Long) queryUnique(hql.toString(), values.toArray());
	}

	@Override
	public List<StudentInfoModel> queryStudentInfoRegistedByCollege(
			Dic yearDic, BaseAcademyModel college, String flag) {
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentInfoModel t where 1=1 ");

		hql.append(" and  t.enterYearDic.id = ? ");
		values.add(yearDic.getId());
		if (flag != null) {
			hql.append(" and  t.status = ? ");
			values.add(Constants.REGULATORY_NEED);
		}
		hql.append(" and  t.college.id = ? ");
		values.add(college.getId());

		List<StudentInfoModel> list = (List<StudentInfoModel>) query(
				hql.toString(), values.toArray());
		return (list != null && list.size() > 0) ? list : null;
	}

	@Override
	public List<String> getProvinceDicByEnterYear(String yearId) {
		sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"update HKY_STUDENT_INFO SET CANDIDATE_PROVENCE = (select id from DIC WHERE CODE =(SUBSTR(CANDIDATE_NUM, 3, 2))) WHERE LENGTH(CANDIDATE_PROVENCE) != 32")
				.executeUpdate(); // 修改生源地字段

		sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"update HKY_STUDENT_INFO SET CANDIDATE_PROVENCE = '"
								+ DicFactory
										.getDicUtil()
										.getDicInfo("DIC_CANDIDATE_PROVENCE",
												"OTHER_NATIVE").getId()
								+ "' where CANDIDATE_PROVENCE  is null")
				.executeUpdate(); // 修改生源地字段

		StringBuffer hql = new StringBuffer(
				" select t.candidateProvence.code from StudentInfoModel t where t.enterYearDic.id = ? group by t.candidateProvence.code order by t.candidateProvence.code");
		return (List<String>) query(hql.toString(), new String[] { yearId });
	}

	/**
	 * 报到进度查询优化方案
	 * 
	 * @param yearId
	 * @return
	 */
	@Override
	@Deprecated
	public List<Object[]> getReportProgress(String yearId, List<String> ls,
			String range, String collegeId, String majorId, String klassId) {
		StringBuffer sqlSb = new StringBuffer();
		List<Object> values = new ArrayList<Object>();
		if ("1".equals(range)) {
			sqlSb.append("select max(c.name), sum(case when t.status = '1' or t.status != '1' then 1 else 0 end), sum(case when t.status = '1' then 1 else 0 end)");
		} else if ("2".equals(range)) {
			sqlSb.append("select max(c.name), max(m.major_name), sum(case when t.status = '1' or t.status != '1' then 1 else 0 end), sum(case when t.status = '1' then 1 else 0 end)");
		} else {
			sqlSb.append("select max(c.name), max(m.major_name), max(k.class_name), sum(case when t.status = '1' or t.status != '1' then 1 else 0 end), sum(case when t.status = '1' then 1 else 0 end)");
		}
		for (int i = 0; i < ls.size(); i++) {
			String d = ls.get(i);
			sqlSb.append(", sum(case when t.CANDIDATE_PROVENCE= '" + d
					+ "' then 1 else 0 end ) ");
			sqlSb.append(", sum(case when t.source_land= '" + d
					+ "' and t.status = '1' then 1 else 0 end ) ");
		}
		if ("1".equals(range)) {
			sqlSb.append(" from HKY_STUDENT_INFO t left join HKY_BASE_COLLAGE c on t.college = c.id where t.enter_year = ? ");
			values.add(yearId);
			if (DataUtil.isNotNull(collegeId)) {
				sqlSb.append(" and t.college = ? ");
				values.add(collegeId);
			}
			sqlSb.append(" group by t.college order by t.college ");
		} else if ("2".equals(range)) {
			sqlSb.append(" from HKY_STUDENT_INFO t left join HKY_BASE_COLLAGE c on t.college = c.id "
					+ "left join HKY_BASE_MAJOR m on m.id = t.major where t.enter_year = ? ");
			values.add(yearId);
			if (DataUtil.isNotNull(majorId)) {
				sqlSb.append(" and t.major = ? ");
				values.add(majorId);
			} else if (DataUtil.isNotNull(collegeId)
					&& DataUtil.isNull(majorId)) {
				sqlSb.append(" and t.college = ? ");
				values.add(collegeId);
			}
			sqlSb.append(" group by t.major order by t.major ");
		} else {
			sqlSb.append(" from HKY_STUDENT_INFO t left join HKY_BASE_COLLAGE c on t.college = c.id "
					+ "left join HKY_BASE_MAJOR m on m.id = t.major "
					+ "left join HKY_BASE_CLASS k on t.class_id = k.id where t.enter_year = ? ");
			values.add(yearId);
			if (DataUtil.isNotNull(klassId)) {
				sqlSb.append(" and t.class_id = ? ");
				values.add(klassId);
			} else if (DataUtil.isNotNull(majorId) && DataUtil.isNull(klassId)) {
				sqlSb.append(" and t.major = ? ");
				values.add(majorId);
			} else if (DataUtil.isNotNull(collegeId)
					&& DataUtil.isNull(klassId) && DataUtil.isNull(klassId)) {
				sqlSb.append(" and t.college = ? ");
				values.add(collegeId);
			}
			sqlSb.append(" group by t.class_id order by t.class_id ");
		}
		return this.querySQL(sqlSb.toString(), values.toArray());
	}

	@Override
	public List<Object[]> getOtherProvinceCount(String yearId,
			String collegeId, String majorId, String klassId, String flag) {
		StringBuffer sqlSb = new StringBuffer(
				"select sum(case when t.status = '1' then 1 else 0 end),"
						+ " sum(case when (t.status != '1' or t.status = '1') then 1 else 0 end)"
						+ " from hky_student_info t where t.enter_year = ? ");
		List<Object> values = new ArrayList<Object>();
		values.add(yearId);
		if (flag != null) {
			if (DataUtil.isNotNull(klassId)) {
				sqlSb.append(" and t.class_id = ? ");
				values.add(klassId);
			} else if (DataUtil.isNull(klassId) && DataUtil.isNotNull(majorId)) {
				sqlSb.append(" and t.major = ? ");
				values.add(majorId);
			} else if (DataUtil.isNull(klassId) && DataUtil.isNull(majorId)
					&& DataUtil.isNotNull(collegeId)) {
				sqlSb.append(" and t.college = ? ");
				values.add(collegeId);
			}
			sqlSb.append(" and t.source_land != ? ");
			// 数据库中 浙江code
			values.add("330000");
		}
		return this.querySQL(sqlSb.toString(), values.toArray());
	}

	/**
	 * 查询预报道数据导出的结果
	 * 
	 * @Description: TODO
	 * @author: 唐靖
	 * @date: 2016-11-21 下午2:34:05
	 * @param exportSize
	 * @param exportPage
	 * @param studentReportModel
	 * @return
	 */
	public Page queryExReportList(String exportSize, String exportPage,
			StudentReportModel studentReportModel) {
		sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"update HKY_STUDENT_INFO SET CANDIDATE_PROVENCE = (select id from DIC WHERE CODE =(SUBSTR(CANDIDATE_NUM, 3, 2))) WHERE LENGTH(CANDIDATE_PROVENCE) != 32")
				.executeUpdate(); // 修改生源地字段

		sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"update HKY_STUDENT_INFO SET CANDIDATE_PROVENCE = '"
								+ DicFactory
										.getDicUtil()
										.getDicInfo("DIC_CANDIDATE_PROVENCE",
												"OTHER_NATIVE").getId()
								+ "' where CANDIDATE_PROVENCE  is null")
				.executeUpdate(); // 修改生源地字段
		List<Object> values = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
				" from StudentReportModel t where 1=1 ");
		if (null != studentReportModel) {
			// 学年
			if (studentReportModel.getYearDic() != null
					&& studentReportModel.getYearDic().getId() != null
					&& !studentReportModel.getYearDic().getId().equals("")) {
				hql.append(" and t.yearDic.id = ? ");
				values.add(studentReportModel.getYearDic().getId());
			}
			// 获取学生基本信息实体类
			if (studentReportModel.getStudentInfo() != null) {

				StudentInfoModel studentInfo = studentReportModel
						.getStudentInfo();
				// 学院
				if (studentInfo.getCollege() != null
						&& studentInfo.getCollege().getId() != null
						&& !studentInfo.getCollege().getId().equals("")) {
					hql.append(" and  t.studentInfo.college.id = ? ");
					values.add(studentInfo.getCollege().getId());
				}
				// 专业
				if (studentInfo.getMajor() != null
						&& studentInfo.getMajor().getId() != null
						&& !studentInfo.getMajor().getId().equals("")) {
					hql.append(" and  t.studentInfo.major.id = ? ");
					values.add(studentInfo.getMajor().getId());
				}
				// 班级
				if (studentInfo.getClassId() != null
						&& studentInfo.getClassId().getId() != null
						&& !studentInfo.getClassId().getId().equals("")) {
					hql.append(" and  t.studentInfo.classId.id = ? ");
					values.add(studentInfo.getClassId().getId());
				}
				// 姓名（模糊查询）
				if (studentInfo.getName() != null
						&& !studentInfo.getName().trim().equals("")) {
					hql.append(" and  t.studentInfo.name like ? ");
					values.add("%" + studentInfo.getName().trim() + "%");
				}
				// 学号
				/**
				 * if(studentInfo.getStuNumber()!=null &&
				 * !studentInfo.getStuNumber().trim().equals("")) {
				 * hql.append(" and  t.studentInfo.stuNumber like ? ");
				 * values.add("%" + studentInfo.getStuNumber().trim() + "%"); }
				 */
				// 身份证号(证件号码)
				/**
				 * if(studentInfo.getCertificateCode()!=null &&
				 * !studentInfo.getCertificateCode().trim().equals("")) {
				 * hql.append(" and  t.studentInfo.certificateCode like ? ");
				 * values.add("%" + studentInfo.getCertificateCode().trim() +
				 * "%"); }
				 */
			}
			// 是否报到
			if (DataUtil.isNotNull(studentReportModel.getIsReport())) {
				hql.append(" and  t.isReport = ? ");
				values.add(studentReportModel.getIsReport());
			}

			// 开始时间
			if (studentReportModel.getReportStartDate() != null) {
				hql.append(" and  t.reportDate >= ? ");
				values.add(studentReportModel.getReportStartDate());
			}
			// 结束时间
			if (studentReportModel.getReportEndDate() != null) {
				hql.append(" and  t.reportDate <= ? ");
				values.add(studentReportModel.getReportEndDate());
			}
			// 乘车方式
			if (studentReportModel.getRideWayDic() != null
					&& studentReportModel.getRideWayDic().getId() != null
					&& !studentReportModel.getRideWayDic().getId().equals("")) {
				hql.append(" and  t.rideWayDic.id = ? ");
				values.add(studentReportModel.getRideWayDic().getId());
			}
			// 到达站点
			if (studentReportModel.getSiteDic() != null
					&& studentReportModel.getSiteDic().getId() != null
					&& !studentReportModel.getSiteDic().getId().equals("")) {
				hql.append(" and  t.siteDic.id = ? ");
				values.add(studentReportModel.getSiteDic().getId());
			}
			// 需要空调
			/**
			 * if (studentReportModel.getAirCond()!=null &&
			 * !studentReportModel.getAirCond().equals("")) {
			 * hql.append(" and  t.airCond = ? ");
			 * values.add(studentReportModel.getAirCond()); }
			 */
			// 寝具包
			if (studentReportModel.getBed() != null
					&& !studentReportModel.getBed().equals("")) {
				hql.append(" and  t.bed = ? ");
				values.add(studentReportModel.getBed());
			}
			// 迁户口
			if (studentReportModel.getMove() != null
					&& !studentReportModel.getMove().equals("")) {
				hql.append(" and  t.move = ? ");
				values.add(studentReportModel.getMove());
			}
		}
		// 排序条件
		hql.append(" order by updateTime desc ");

		return this.pagedQuery(hql.toString(), Integer.valueOf(exportPage),
				Integer.valueOf(exportSize), values.toArray());

	}

	@Override
	public List<Object[]> getReportProgressByProDic(String yearId,
			List<Dic> ls, String range, String collegeId, String majorId,
			String klassId) {
		StringBuffer sqlSb = new StringBuffer();
		List<Object> values = new ArrayList<Object>();
		if ("1".equals(range)) {
			sqlSb.append("select max(c.name), sum(case when t.status = '1' or t.status != '1' then 1 else 0 end), sum(case when t.status = '1' then 1 else 0 end)");
		} else if ("2".equals(range)) {
			sqlSb.append("select max(c.name), max(m.major_name), sum(case when t.status = '1' or t.status != '1' then 1 else 0 end), sum(case when t.status = '1' then 1 else 0 end)");
		} else {
			sqlSb.append("select max(c.name), max(m.major_name), max(k.class_name), sum(case when t.status = '1' or t.status != '1' then 1 else 0 end), sum(case when t.status = '1' then 1 else 0 end)");
		}
		for (int i = 0; i < ls.size(); i++) {
			Dic d = ls.get(i);
			sqlSb.append(", sum(case when t.CANDIDATE_PROVENCE= '" + d.getId()
					+ "' then 1 else 0 end ) ");
			sqlSb.append(", sum(case when t.CANDIDATE_PROVENCE= '" + d.getId()
					+ "' and t.status = '1' then 1 else 0 end ) ");
		}
		if ("1".equals(range)) {
			sqlSb.append(" from HKY_STUDENT_INFO t left join HKY_BASE_COLLAGE c on t.college = c.id where t.enter_year = ? ");
			values.add(yearId);
			if (DataUtil.isNotNull(collegeId)) {
				sqlSb.append(" and t.college = ? ");
				values.add(collegeId);
			}
			sqlSb.append(" group by t.college order by t.college ");
		} else if ("2".equals(range)) {
			sqlSb.append(" from HKY_STUDENT_INFO t left join HKY_BASE_COLLAGE c on t.college = c.id "
					+ "left join HKY_BASE_MAJOR m on m.id = t.major where t.enter_year = ? ");
			values.add(yearId);
			if (DataUtil.isNotNull(majorId)) {
				sqlSb.append(" and t.major = ? ");
				values.add(majorId);
			} else if (DataUtil.isNotNull(collegeId)
					&& DataUtil.isNull(majorId)) {
				sqlSb.append(" and t.college = ? ");
				values.add(collegeId);
			}
			sqlSb.append(" group by t.major order by t.major ");
		} else {
			sqlSb.append(" from HKY_STUDENT_INFO t left join HKY_BASE_COLLAGE c on t.college = c.id "
					+ "left join HKY_BASE_MAJOR m on m.id = t.major "
					+ "left join HKY_BASE_CLASS k on t.class_id = k.id where t.enter_year = ? ");
			values.add(yearId);
			if (DataUtil.isNotNull(klassId)) {
				sqlSb.append(" and t.class_id = ? ");
				values.add(klassId);
			} else if (DataUtil.isNotNull(majorId) && DataUtil.isNull(klassId)) {
				sqlSb.append(" and t.major = ? ");
				values.add(majorId);
			} else if (DataUtil.isNotNull(collegeId)
					&& DataUtil.isNull(klassId) && DataUtil.isNull(klassId)) {
				sqlSb.append(" and t.college = ? ");
				values.add(collegeId);
			}
			sqlSb.append(" group by t.class_id order by t.class_id ");
		}
		return this.querySQL(sqlSb.toString(), values.toArray());
	}
}
