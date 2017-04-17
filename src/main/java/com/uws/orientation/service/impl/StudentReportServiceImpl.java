package com.uws.orientation.service.impl;

import java.util.Date;

import org.apache.tools.ant.util.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.uws.common.service.IBaseDataService;
import com.uws.common.util.AmsDateUtil;
import com.uws.common.util.SchoolYearUtil;
import com.uws.core.base.BaseServiceImpl;
import com.uws.core.util.DataUtil;
import com.uws.core.util.DateUtil;
import com.uws.domain.base.BaseRoomModel;
import com.uws.domain.base.StudentRoomModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.orientation.StudentReportModel;
import com.uws.orientation.dao.IStudentInfoDao;
import com.uws.orientation.dao.IStudentReportDao;
import com.uws.orientation.service.IStudentReportService;
import com.uws.webservice.IFeeServcie;

/**
 * 
* @ClassName: IStudentReportService 
* @Description: TODO(学生预报到信息的Service) 
* @author wangcl
* @date 2015-7-23 下午9:30:48 
*
 */
@Repository("studentReportServiceImpl")
public class StudentReportServiceImpl extends BaseServiceImpl implements
IStudentReportService {

	// 学生预报到信息Dao
	@Autowired
	private IStudentReportDao studentReportDao;
	
	// 学生基本信息Dao
	@Autowired
	private IStudentInfoDao studentInfoDao;
	
	// 基础信息查询
	@Autowired
	private IBaseDataService baseDataService;
	
	// 根据财务的接口
	@Autowired
	private IFeeServcie feeServcie;
	
	/**
	 * 
	* @Title: getByStudent 
	* @Description: TODO(通过学生Id查询出预报到信息) 
	* @param  studentId 学生Id
	* @return StudentReportModel
	* @author wangcl
	 */
	public StudentReportModel getByStudentId(String studentId){
		return studentReportDao.getByStudentId(studentId);
		
	}
	
	/**
	 * 
	* @Title: saveStudentReport 
	* @Description: TODO(保存学生预报到信息) 
	* @param  StudentReportModel 预报到Model
	* @return void
	* @author wangcl
	 */
	public void saveStudentReport( StudentReportModel studentReport ){
		// TODO Auto-generated method stub
		//生成UUID
		String id = DataUtil.createOID();
		//设置Id
		studentReport.setId(id);
		
		//预报到时间
		String timeStr = studentReport.getReportDateStr();
		if (!org.apache.commons.lang.StringUtils.isBlank(timeStr)) {
			studentReport.setReportDate(AmsDateUtil.toTime(timeStr));
		}
		
		//年份
		//studentReport.setYear(DateUtils.format(new Date(), "yyyy"));
		//String enterYear= DateUtils.format(new Date(), "yyyy");
		//对应学年
		studentReport.setYearDic(SchoolYearUtil.getYearDic());
		
		//创建时间
		studentReport.setCreateTime(new Date());
		//状态
		studentReport.setStatus("0");
				
		//保存
	    studentReportDao.save(studentReport);
	}
	
	/**
	 * 
	* @Title: updateStudentReport 
	* @Description: TODO(修改学生预报到信息) 
	* @param  StudentReportModel 学生预报到Model
	* @return void
	* @author wangcl
	 */
	public void updateStudentReport( StudentReportModel studentReport ){
		// 取得 学生监护人信息的Po
		StudentReportModel poTemp = studentReportDao.getByStudentId(studentReport.getStudentInfo().getId());
		//预报到时间
		/*String dateStr = studentReport.getReportDateStr();
		if (!org.apache.commons.lang.StringUtils.isBlank(dateStr)) {
			studentReport.setReportDate(DateUtil.toDate(dateStr));
			poTemp.setReportDate(DateUtil.toDate(dateStr));
		}*/
		//预报到时间
		String timeStr = studentReport.getReportDateStr();
		if (!org.apache.commons.lang.StringUtils.isBlank(timeStr)) {
					studentReport.setReportDate(AmsDateUtil.toTime(timeStr));
		}
		//spring的对象的属性copy
		BeanUtils.copyProperties(studentReport, poTemp, new String[] {"id","createTime","status" });	
		
		//年份
		//studentReport.setYear(DateUtils.format(new Date(), "yyyy"));
		//String enterYear= DateUtils.format(new Date(), "yyyy");
		//对应学年
		poTemp.setYearDic(SchoolYearUtil.getYearDic());
		
		//修改时间
		poTemp.setUpdateTime(new Date());
		//状态
		//studentReport.setStatus("0");
		
		// 进行数据库更新
		studentReportDao.update(poTemp);
	}
	
}
