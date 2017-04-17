package com.uws.orientation.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import com.uws.core.base.IBaseService;
import com.uws.core.excel.ExcelException;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.sys.model.Dic;

/**
 * 
 * @ClassName: IStudentInfoService
 * @Description: TODO(学生基本信息的Service)
 * @author wangcl
 * @date 2015-7-23 上午9:46:48
 * 
 */
public interface IStudentInfoService extends IBaseService {

	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(通过证件号、密码和验证码进行登录系统)
	 * @param certificatCode
	 *            证件号码
	 * @param password
	 *            密码
	 * @param code
	 *            验证码
	 * @return studentInfo
	 * @author wangcl
	 */
	public void loginAuth(String certificateCode, String passWord, String code)
			throws Exception;

	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(通过证件号和当前学年查询出学生信息)
	 * @param certificatCode
	 *            证件号码
	 * @return studentInfo
	 * @author wangcl
	 */
	public StudentInfoModel getByCertificatCodeAndYear(String certificateCode);

	/**
	 * 根据记录号（id）进行学生基本信息查询
	 * 
	 * @param id
	 *            学生基本信息记录号
	 * @return 提前离校申请Po
	 */
	public StudentInfoModel getStudentInfoById(String id);

	/**
	 * 
	 * @Title: updateStudentInfo
	 * @Description: TODO(修改学生监护人信息)
	 * @param studentGuardian
	 *            学生监护人Model
	 * @return void
	 * @author wangcl
	 */
	public void updateStudentInfo(StudentInfoModel studentInfo);

	/**
	 * 
	 * @Title: queryCheckNewStudentPassword
	 * @Description: TODO(检查学生的旧密码是否正确)
	 * @param id
	 *            学生Id
	 * @param oldPassWord
	 *            学生新密码
	 * @return boolean
	 * @author wangcl
	 */
	public boolean queryCheckNewStudentPassword(String id, String oldPassWord)
			throws NoSuchAlgorithmException;

	/**
	 * 
	 * @Title: updateNewStudentPassWord
	 * @Description: TODO(修改学生登录信息采集系统的密码信息)
	 * @param id
	 *            学生Id
	 * @param newPassWord
	 *            学生新密码
	 * @return void
	 * @author wangcl
	 */
	public void updateNewStudentPassWord(String id, String newPassWord)
			throws NoSuchAlgorithmException;

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
	public Page pageQueryStudentReport(int pageSize, int pageNo,
			StudentInfoModel po);

	/**
	 * 新生报到办理查询导出
	 * 
	 * @param StudentInfoModel
	 *            学生基本信息Po
	 *  @param
	 *             
	 * @return List
	 */
	public List<StudentInfoModel> getStudentInfoByReport(StudentInfoModel po);

	/**
	 * 
	 * @Title: updateStudentReportStatus
	 * @Description: TODO(对新生报到状态的更新)
	 * @param id
	 *            学生Id
	 * @param status
	 *            报到状态
	 * @param siteDic
	 *            报到点
	 * @param cancelReason
	 *            撤销原因
	 * @return void
	 * @author wangcl
	 */
	public void updateStudentReportStatus(String id, String status,
			Dic siteDic, String cancelReason);

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
	public Page pageQueryGreenWay(int pageSize, int pageNo, StudentInfoModel po);

	/**
	 * 
	 * @Title: updateGreenWayStatus
	 * @Description: TODO(对新生绿色通道办理状态的更新)
	 * @param id
	 *            学生Id
	 * @param status
	 *            绿色通道状态
	 * @param reasonDic
	 *            原因
	 * @return void
	 * @author wangcl
	 */
	public void updateGreenWayStatus(String id, String status, Dic reasonDic);

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
	public Page pageQueryCancelReport(int pageSize, int pageNo,
			StudentInfoModel po);

	/**
	 * 新生撤销报到办理查询
	 * 
	 * @param stuNumber
	 *            学号
	 * @return boolean
	 */
	public boolean isExistStudent(String stuNumber);

	/**
	 * @Title: importData
	 * @Description: TODO(新生信息导入的处理)
	 * @param @param filePath
	 * @param @param importId
	 * @param @param initDate
	 * @param @param c 映射用model
	 * @param @return
	 * @param @throws ExcelException
	 * @param @throws InstantiationException
	 * @param @throws IOException
	 * @param @throws IllegalAccessException
	 * @param @throws ClassNotFoundException
	 * @return String
	 * @throws
	 * @author wangcl
	 */
	public String importData(String filePath, String importId, Map initDate,
			Class c) throws ExcelException, InstantiationException,
			IOException, IllegalAccessException, ClassNotFoundException,
			InvocationTargetException;
	/**
	 * 根据学年批量修改缴费状态
	 * 
	 * @param 学年
	 * 
	 * @return boolean
	 */
	public boolean updateAllCost(Dic yearDic);

}


