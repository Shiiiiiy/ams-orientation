package com.uws.orientation.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.uws.common.util.SchoolYearUtil;
import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.util.DataUtil;
import com.uws.core.util.HqlEscapeUtil;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.orientation.dao.IStudentInfoDao;
import com.uws.sys.model.Dic;

/**
 * 
 * @ClassName: StudentInfoDaoImol
 * @Description: TODO(学生基本信息的DaoImpl)
 * @author wangcl
 * @date 2015-7-23 上午9:46:48
 * 
 */
@Repository("studentInfoDaoImpl")
public class StudentInfoDaoImpl extends BaseDaoImpl implements IStudentInfoDao {

	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(通过证件号和密码查询出学生信息)
	 * @param certificatCode
	 *            证件号码
	 * @param password
	 *            密码
	 * @return studentInfo
	 * @author wangcl
	 */
	@Override
	public StudentInfoModel getByCertificatCodeAndPassWord(
			String certificateCode, String passWord) {
		// 当前的学年
		Dic yearDic = SchoolYearUtil.getYearDic();

		// TODO Auto-generated method stub
		String hql = "from StudentInfoModel stu where stu.certificateCode = ? and stu.passWord = ? and stu.enterYearDic.id = ?";
		Object object = this.queryUnique(hql, new Object[] { certificateCode,
				passWord, yearDic.getId() });

		return DataUtil.isNotNull(object) ? (StudentInfoModel) object : null;
	}

	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(通过证件号和当前学年查询出学生信息)
	 * @param certificatCode
	 *            证件号码
	 * @return studentInfo
	 * @author wangcl
	 */
	@Override
	public StudentInfoModel getByCertificatCodeAndYear(String certificateCode) {
		// 当前的学年
		Dic yearDic = SchoolYearUtil.getYearDic();

		// TODO Auto-generated method stub
		String hql = "from StudentInfoModel stu where stu.certificateCode = ? and stu.enterYearDic.id = ?";
		Object object = this.queryUnique(hql, new Object[] { certificateCode,
				yearDic.getId() });

		return DataUtil.isNotNull(object) ? (StudentInfoModel) object : null;
	}

	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(通过证件号学生信息)
	 * @param registerNo
	 *            证件号码
	 * @return studentInfo
	 * @author wangcl
	 */
	@Override
	public StudentInfoModel getByCertificatCode(String certificateCode) {
		// TODO Auto-generated method stub

		String hql = "from StudentInfoModel stu where stu.certificateCode = ?";

		Object object = this.queryUnique(hql, certificateCode);

		return DataUtil.isNotNull(object) ? (StudentInfoModel) object : null;
	}

	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(通过学生学号取得学生信息)
	 * @param stuNumber
	 *            证件号码
	 * @return studentInfo
	 * @author wangcl
	 */
	@Override
	public StudentInfoModel getByStudetnNumber(String stuNumber) {
		// TODO Auto-generated method stub

		String hql = "from StudentInfoModel stu where stu.stuNumber = ?";

		Object object = this.queryUnique(hql, stuNumber);

		return DataUtil.isNotNull(object) ? (StudentInfoModel) object : null;
	}

	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(查询检查学生密码信息)
	 * @param id
	 *            学生基本信息记录号
	 * @param oldPassWord
	 *            学生旧密码
	 * @return boolean
	 * @author wangcl
	 */
	@Override
	public boolean queryCheckPassword(String id, String oldPassWord) {
		String hql = "from StudentInfoModel stu where stu.id = ? and stu.passWord = ?";

		return this.queryUnique(hql, new String[] { id, oldPassWord }) == null ? false
				: true;
	}

	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(修改新密码)
	 * @param id
	 *            学生基本信息记录号
	 * @param newPassWord
	 *            学生新密码
	 * @return void
	 * @author wangcl
	 */
	@Override
	public void updatePassWord(String id, String newPassWord) {
		// TODO Auto-generated method stub
		String hql = "update StudentInfoModel stu set stu.passWord = ? where stu.id = ?";
		this.executeHql(hql, new String[] { newPassWord, id });
	}

	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(根据学生Id，修改缴费状态)
	 * @param id
	 *            学生基本信息记录号
	 * @param 缴费状态
	 *            costState
	 * @return void
	 * @author wangcl
	 */
	@Override
	public void updateStudentCostState(String id, String costState) {
		// TODO Auto-generated method stub
		String hql = "update StudentInfoModel stu set stu.costState = ? where stu.id = ?";
		this.executeHql(hql, new String[] { costState, id });
	}

	/**
	 * 新生报到办理查询
	 * 
	 * @param pageNo
	 *            页数
	 * @param pageSize
	 *            每页有几条记录
	 * @param StudentInfoModel
	 *            学生基本信息Po
	 * @return page
	 */
	@Override
	public Page pageQueryStudentReport(int pageSize, int pageNo,
			StudentInfoModel po) {

		List<String> values = new ArrayList<String>();
		// 查询的初始语句
		StringBuffer hql = new StringBuffer(
				"select sim from StudentInfoModel sim where 1=1 ");

		// 学院
		if (po.getCollege() != null
				&& StringUtils.isNotBlank(po.getCollege().getId())) {
			hql.append(" and sim.college.id = ?");
			values.add(po.getCollege().getId());
		}

		// 专业
		if (po.getMajor() != null
				&& StringUtils.isNotBlank(po.getMajor().getId())) {
			hql.append(" and sim.major.id = ?");
			values.add(po.getMajor().getId());
		}

		// 班级
		if (po.getClassId() != null
				&& StringUtils.isNotBlank(po.getClassId().getId())) {
			hql.append(" and sim.classId.id = ?");
			values.add(po.getClassId().getId());
		}

		// 姓名
		if (StringUtils.isNotBlank(po.getName())) {
			hql.append(" and sim.name like '%"
					+ HqlEscapeUtil.escape(po.getName()) + "%' ");
		}

		// 学号
		if (StringUtils.isNotBlank(po.getStuNumber())) {
			hql.append(" and sim.stuNumber like '%"
					+ HqlEscapeUtil.escape(po.getStuNumber()) + "%' ");
		}

		// 证件号
		if (StringUtils.isNotBlank(po.getCertificateCode())) {
			hql.append(" and sim.certificateCode like '%"
					+ HqlEscapeUtil.escape(po.getCertificateCode()) + "%' ");
		}

		// 班级
		if (StringUtils.isNotBlank(po.getCertificateCode())) {
			hql.append(" and sim.classId.id = ?");
			values.add(po.getClassId().getId());
		}

		// 绿色通道
		if (po.getGreenWay() != null
				&& StringUtils.isNotBlank(po.getGreenWay())) {
			hql.append(" and sim.greenWay = ?");
			values.add(po.getGreenWay());
		}

		// 状态
		if (po.getStatus() != null && StringUtils.isNotBlank(po.getStatus())) {
			hql.append(" and sim.status = ?");
			values.add(po.getStatus());
		}

		// 入学年份
		/*
		 * if (po.getEnterYear()!=null &&
		 * StringUtils.isNotBlank(po.getEnterYear())) {
		 * hql.append(" and sim.enterYear = ?"); values.add(po.getEnterYear());
		 * }
		 */
		if (po.getEnterYearDic() != null
				&& StringUtils.isNotBlank(po.getEnterYearDic().getId())) {
			hql.append(" and sim.enterYearDic.id = ?");
			values.add(po.getEnterYearDic().getId());
		}

		// 按照创建日期降序进行排序
		hql.append(" order by sim.createTime asc");
		// 进行数据库查询并放回结果集。
		return values.size() > 0 ? this.pagedQuery(hql.toString(), pageNo,
				pageSize, values.toArray()) : this.pagedQuery(hql.toString(),
				pageNo, pageSize);
	}

	/**
	 * 新生报到办理查询导出
	 * 
	 * @param StudentInfoModel
	 *            学生基本信息Po
	 * @param 标识
	 *            判断是收费状态，
	 * 
	 * @return List
	 */
	@Override
	public List<StudentInfoModel> getStudentInfoByReport(StudentInfoModel po,
			String flag) {

		List<String> values = new ArrayList<String>();
		// 查询的初始语句
		StringBuffer hql = new StringBuffer(
				"select sim from StudentInfoModel sim where 1=1 ");

		// 学院
		if (po.getCollege() != null
				&& StringUtils.isNotBlank(po.getCollege().getId())) {
			hql.append(" and sim.college.id = ?");
			values.add(po.getCollege().getId());
		}

		// 专业
		if (po.getMajor() != null
				&& StringUtils.isNotBlank(po.getMajor().getId())) {
			hql.append(" and sim.major.id = ?");
			values.add(po.getMajor().getId());
		}

		// 班级
		if (po.getClassId() != null
				&& StringUtils.isNotBlank(po.getClassId().getId())) {
			hql.append(" and sim.classId.id = ?");
			values.add(po.getClassId().getId());
		}

		// 姓名
		if (StringUtils.isNotBlank(po.getName())) {
			hql.append(" and sim.name like '%"
					+ HqlEscapeUtil.escape(po.getName()) + "%' ");
		}

		// 学号
		if (StringUtils.isNotBlank(po.getStuNumber())) {
			hql.append(" and sim.stuNumber like '%"
					+ HqlEscapeUtil.escape(po.getStuNumber()) + "%' ");
		}

		// 证件号
		if (StringUtils.isNotBlank(po.getCertificateCode())) {
			hql.append(" and sim.certificateCode like '%"
					+ HqlEscapeUtil.escape(po.getCertificateCode()) + "%' ");
		}

		// 班级
		if (StringUtils.isNotBlank(po.getCertificateCode())) {
			hql.append(" and sim.classId.id = ?");
			values.add(po.getClassId().getId());
		}

		// 状态
		if (po.getStatus() != null && StringUtils.isNotBlank(po.getStatus())) {
			hql.append(" and sim.status = ?");
			values.add(po.getStatus());
		}

		// 绿色通道
		if (po.getGreenWay() != null
				&& StringUtils.isNotBlank(po.getGreenWay())) {
			hql.append(" and sim.greenWay = ?");
			values.add(po.getGreenWay());
		}

		// 入学年份
		/*
		 * if (po.getEnterYear() != null &&
		 * StringUtils.isNotBlank(po.getEnterYear())) {
		 * hql.append(" and sim.enterYear = ?"); values.add(po.getEnterYear());
		 * }
		 */
		if (po.getEnterYearDic() != null
				&& StringUtils.isNotBlank(po.getEnterYearDic().getId())) {
			hql.append(" and sim.enterYearDic.id = ?");
			values.add(po.getEnterYearDic().getId());
		}

		if (flag != null) {
			// 缴费状态
			hql.append(" and sim.costState != ?");
			values.add("1");

		}

		// 按照创建日期降序进行排序
		hql.append(" order by sim.createTime asc");
		// 进行数据库查询并放回结果集。
		return this.query(hql.toString(), values.toArray());
		// return values.size()>0?this.pagedQuery(hql.toString(), pageNo,
		// pageSize, values.toArray()):this.pagedQuery(hql.toString(), pageNo,
		// pageSize);
	}

	/**
	 * 新生绿色通道办理查询
	 * 
	 * @param pageNo
	 *            页数
	 * @param pageSize
	 *            每页有几条记录
	 * @param StudentInfoModel
	 *            学生基本信息Po
	 * @return page
	 */
	@Override
	public Page pageQueryGreenWay(int pageSize, int pageNo, StudentInfoModel po) {

		List<String> values = new ArrayList<String>();
		// 查询的初始语句
		StringBuffer hql = new StringBuffer(
				"select sim from StudentInfoModel sim where 1=1 ");

		// 学院
		if (po.getCollege() != null
				&& StringUtils.isNotBlank(po.getCollege().getId())) {
			hql.append(" and sim.college.id = ?");
			values.add(po.getCollege().getId());
		}

		// 专业
		if (po.getMajor() != null
				&& StringUtils.isNotBlank(po.getMajor().getId())) {
			hql.append(" and sim.major.id = ?");
			values.add(po.getMajor().getId());
		}

		// 班级
		if (po.getClassId() != null
				&& StringUtils.isNotBlank(po.getClassId().getId())) {
			hql.append(" and sim.classId.id = ?");
			values.add(po.getClassId().getId());
		}

		// 姓名
		if (StringUtils.isNotBlank(po.getName())) {
			hql.append(" and sim.name like '%"
					+ HqlEscapeUtil.escape(po.getName()) + "%' ");
		}

		// 学号
		if (StringUtils.isNotBlank(po.getStuNumber())) {
			hql.append(" and sim.stuNumber like '%"
					+ HqlEscapeUtil.escape(po.getStuNumber()) + "%' ");
		}

		// 证件号
		if (StringUtils.isNotBlank(po.getCertificateCode())) {
			hql.append(" and sim.certificateCode like '%"
					+ HqlEscapeUtil.escape(po.getCertificateCode()) + "%' ");
		}

		// 班级
		if (StringUtils.isNotBlank(po.getCertificateCode())) {
			hql.append(" and sim.classId.id = ?");
			values.add(po.getClassId().getId());
		}

		// 绿色通道
		if (po.getGreenWay() != null
				&& StringUtils.isNotBlank(po.getGreenWay())) {
			hql.append(" and sim.greenWay = ?");
			values.add(po.getGreenWay());
		}

		// 状态

		if (po.getStatus() != null && StringUtils.isNotBlank(po.getStatus())) {
			hql.append(" and sim.status = ?");
			values.add(po.getStatus());
		} else {

		}
		// 入学年份
		if (po.getEnterYearDic() != null
				&& StringUtils.isNotBlank(po.getEnterYearDic().getId())) {
			hql.append(" and sim.enterYearDic.id = ?");
			values.add(po.getEnterYearDic().getId());
		}

		// hql.append(" and (sim.status != '1' or sim.status !='2') ");
		// hql.append(" and sim.status = '0' ");
		hql.append(" and sim.status in ('0','1','2') ");

		// 按照创建日期降序进行排序
		hql.append(" order by sim.createTime asc");
		// 进行数据库查询并放回结果集。
		return values.size() > 0 ? this.pagedQuery(hql.toString(), pageNo,
				pageSize, values.toArray()) : this.pagedQuery(hql.toString(),
				pageNo, pageSize);
	}

	/**
	 * 新生撤销报到办理查询
	 * 
	 * @param pageNo
	 *            页数
	 * @param pageSize
	 *            每页有几条记录
	 * @param StudentInfoModel
	 *            学生基本信息Po
	 * @return page
	 */
	@Override
	public Page pageQueryCancelReport(int pageSize, int pageNo,
			StudentInfoModel po) {

		List<String> values = new ArrayList<String>();
		// 查询的初始语句
		StringBuffer hql = new StringBuffer(
				"select sim from StudentInfoModel sim where 1=1 ");

		// 学院
		if (po.getCollege() != null
				&& StringUtils.isNotBlank(po.getCollege().getId())) {
			hql.append(" and sim.college.id = ?");
			values.add(po.getCollege().getId());
		}

		// 专业
		if (po.getMajor() != null
				&& StringUtils.isNotBlank(po.getMajor().getId())) {
			hql.append(" and sim.major.id = ?");
			values.add(po.getMajor().getId());
		}

		// 班级
		if (po.getClassId() != null
				&& StringUtils.isNotBlank(po.getClassId().getId())) {
			hql.append(" and sim.classId.id = ?");
			values.add(po.getClassId().getId());
		}

		// 姓名
		if (StringUtils.isNotBlank(po.getName())) {
			hql.append(" and sim.name like '%"
					+ HqlEscapeUtil.escape(po.getName()) + "%' ");
		}

		// 学号
		if (StringUtils.isNotBlank(po.getStuNumber())) {
			hql.append(" and sim.stuNumber like '%"
					+ HqlEscapeUtil.escape(po.getStuNumber()) + "%' ");
		}

		// 证件号
		if (StringUtils.isNotBlank(po.getCertificateCode())) {
			hql.append(" and sim.certificateCode like '%"
					+ HqlEscapeUtil.escape(po.getCertificateCode()) + "%' ");
		}

		// 班级
		if (StringUtils.isNotBlank(po.getCertificateCode())) {
			hql.append(" and sim.classId.id = ?");
			values.add(po.getClassId().getId());
		}

		// 绿色通道
		if (po.getGreenWay() != null
				&& StringUtils.isNotBlank(po.getGreenWay())) {
			hql.append(" and sim.greenWay = ?");
			values.add(po.getGreenWay());
		} else {

		}

		// 入学年份
		if (po.getEnterYearDic() != null
				&& StringUtils.isNotBlank(po.getEnterYearDic().getId())) {
			hql.append(" and sim.enterYearDic.id = ?");
			values.add(po.getEnterYearDic().getId());
		}
		// 状态
		if (po.getStatus() != null && StringUtils.isNotBlank(po.getStatus())) {
			hql.append(" and sim.status = ?");
			values.add(po.getStatus());
		} else {
			hql.append(" and sim.status in('1','2') ");
		}

		// 按照创建日期降序进行排序
		hql.append(" order by sim.createTime asc");
		// 进行数据库查询并放回结果集。
		return values.size() > 0 ? this.pagedQuery(hql.toString(), pageNo,
				pageSize, values.toArray()) : this.pagedQuery(hql.toString(),
				pageNo, pageSize);
	}

}
