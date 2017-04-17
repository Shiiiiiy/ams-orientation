package com.uws.orientation.dao;

import java.util.List;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.orientation.StudentInfoModel;

/**
 * 
* @ClassName: IStudentInfoDao 
* @Description: TODO(学生基本信息的Dao) 
* @author wangcl
* @date 2015-7-23 上午8:46:48 
*
 */
public interface IStudentInfoDao extends IBaseDao {
	
	/**
	 * 
	* @Title: queryQuesInfo 
	* @Description: TODO(通过证件号和密码查询出学生信息) 
	* @param  certificatCode 证件号码
	* @param  password 密码
	* @return studentInfo
	* @author wangcl
	 */
	public StudentInfoModel getByCertificatCodeAndPassWord(String certificatCode, String passWord);
	
	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(通过证件号和当前学年查询出学生信息)
	 * @param certificatCode
	 *            证件号码
	 * @return studentInfo
	 * @author wangcl
	 */
	public StudentInfoModel getByCertificatCode(String certificateCode);
	
	/**
	 * 
	* @Title: queryQuesInfo 
	* @Description: TODO(通过学生学号取得学生信息) 
	* @param  stuNumber 证件号码
	* @return studentInfo
	* @author wangcl
	 */
	public StudentInfoModel getByStudetnNumber(String stuNumber);
	
	/**
	 * 
	* @Title: queryQuesInfo 
	* @Description: TODO(通过证件号学生信息) 
	* @param  certificatCode 证件号码
	* @return studentInfo
	* @author wangcl
	 */
	public StudentInfoModel getByCertificatCodeAndYear(String certificatCode);
	
	/**
	 * 
	* @Title: queryQuesInfo 
	* @Description: TODO(查询检查学生密码信息) 
	* @param  id 学生基本信息记录号
	* @param  oldPassWord 学生旧密码
	* @return boolean
	* @author wangcl
	 */
	public boolean queryCheckPassword(String id, String oldPassWord);
	
	/**
	 * 
	* @Title: queryQuesInfo 
	* @Description: TODO(修改新密码) 
	* @param  id 学生基本信息记录号
	* @param  newPassWord 学生新密码
	* @return void 
	* @author wangcl
	 */
	public void updatePassWord(String id, String newPassWord);
	
	/**
	 * 
	 * @Title: queryQuesInfo
	 * @Description: TODO(根据学生Id，修改缴费状态)
	 * @param id
	 *            学生基本信息记录号
	 * @param 缴费状态
	 *            CostState
	 * @return void
	 * @author wangcl
	 */
	public void updateStudentCostState(String id ,String CostState);
	
	/**
	 * 新生报到办理查询
	 * @param pageNo 页数
	 * @param pageSize 每页有几条记录
	 * @param StudentInfoModel 学生基本信息Po
	 * @return page
	 */
	public Page pageQueryStudentReport(int pageSize,int pageNo,StudentInfoModel po);
	
	/**
	 * 新生报到办理查询导出
	 * @param StudentInfoModel 学生基本信息Po
	 *  @param 
	 * @return List
	 */
	public List<StudentInfoModel> getStudentInfoByReport(StudentInfoModel po,String flag);
	
	/**
	 * 新生绿色通道办理查询
	 * @param pageNo 页数
	 * @param pageSize 每页有几条记录
	 * @param StudentInfoModel 学生基本信息Po
	 * @return page
	 */
	public Page pageQueryGreenWay(int pageSize,int pageNo,StudentInfoModel po);

	/**
	 * 新生撤销报到办理查询
	 * @param pageNo 页数
	 * @param pageSize 每页有几条记录
	 * @param StudentInfoModel 学生基本信息Po
	 * @return page
	 */
	public Page pageQueryCancelReport(int pageSize,int pageNo,StudentInfoModel po);
}
