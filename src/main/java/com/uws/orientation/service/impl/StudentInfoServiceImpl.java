package com.uws.orientation.service.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.uws.common.service.IBaseDataService;
import com.uws.core.base.BaseServiceImpl;
import com.uws.core.excel.ExcelException;
import com.uws.core.excel.ImportUtil;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.util.DataUtil;
import com.uws.core.util.DateUtil;
//import com.uws.core.util.MD5;
import com.uws.domain.base.BaseRoomModel;
import com.uws.domain.base.StudentRoomModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.orientation.dao.IStudentInfoDao;
import com.uws.orientation.service.IStudentInfoService;
import com.uws.security.exception.CodeInvalidException;
import com.uws.security.exception.PasswordInvalidException;
//import com.uws.sys.model.Dic;
//import com.uws.sys.service.DicUtil;
//import com.uws.sys.service.impl.DicFactory;
import com.uws.sys.model.Dic;
//import com.uws.sys.service.DicUtil;
//import com.uws.sys.service.impl.DicFactory;
import com.uws.webservice.IFeeServcie;

/**
 * 
 * @ClassName: StudentInfoServiceImpl
 * @Description: TODO(学生基本信息的ServiceImpl)
 * @author wangcl
 * @date 2015-7-23 上午9:46:48
 * 
 */
@Repository("studentLoginService")
public class StudentInfoServiceImpl extends BaseServiceImpl implements
		IStudentInfoService {

	// private DicUtil dicUtil = DicFactory.getDicUtil();

	// 学生基本信息Dao
	@Autowired
	private IStudentInfoDao studentInfoDao;

	// 根据财务的接口
	@Autowired
	private IFeeServcie feeServcie;

	// 基础信息查询
	@Autowired
	private IBaseDataService baseDataService;

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
	@Override
	public void loginAuth(String certificateCode, String passWord, String code)
			throws Exception {
		// TODO Auto-generated method stub
		if ((DataUtil.isNull(certificateCode)) || (DataUtil.isNull(passWord))
				|| (DataUtil.isNull(code))) {
			throw new Exception("证件号 、密码和验证码不能为空");
		}

		HttpSession session = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest().getSession();

		if (!code.equals(session.getAttribute("ccode"))) {
			throw new CodeInvalidException("验证码错误");
		}

		//
		// StudentInfoModel newStudent =
		// studentInfoDao.getByCertificatCodeAndPassWord(certificateCode,
		// MD5.crypt(passWord));
		StudentInfoModel newStudent = studentInfoDao
				.getByCertificatCodeAndPassWord(certificateCode, passWord);

		/*
		 * Dic submitDic = dicUtil.getDicInfo("SUBMIT_STATUS", "NORMAL"); if
		 * (DataUtil.isNotNull(newStudent) && DataUtil.isNotNull(submitDic)) {
		 * if (newStudent.getSubmitStateDic()==null ||
		 * submitDic.getId().equals(newStudent.getSubmitStateDic().getId())) {
		 * throw new Exception("该学生信息未审核通过，暂不能登录"); } }
		 */

		if (DataUtil.isNotNull(newStudent)) {
			session.setAttribute("student_key", newStudent);
		} else {
			throw new PasswordInvalidException("密码不正确");
		}

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
		return getRoomBystudent(getCostState((StudentInfoModel) studentInfoDao
				.getByCertificatCodeAndYear(certificateCode)));

	}

	/**
	 * 根据记录号（id）进行学生基本信息查询
	 * 
	 * @param id
	 *            学生基本信息记录号
	 * @return 提前离校申请Po
	 */
	@Override
	public StudentInfoModel getStudentInfoById(String id) {
		// TODO Auto-generated method stub

		return getRoomBystudent(getCostState((StudentInfoModel) studentInfoDao
				.get(StudentInfoModel.class, id)));

	}

	/**
	 * 
	 * @Title: updateStudentInfo
	 * @Description: TODO(修改学生基本信息信息)
	 * @param studentInfo
	 *            学生基本信息Model
	 * @return void
	 * @author wangcl
	 */
	@Override
	public void updateStudentInfo(StudentInfoModel po) {
		// 取得 学生信息的Po
		StudentInfoModel poTemp = this.getStudentInfoById(po.getId());
		// 姓名汉语拼音
		poTemp.setNamePy(po.getNamePy());

		// 英文名
		poTemp.setEnglishName(po.getEnglishName());

		// 曾用名
		poTemp.setOldName(po.getOldName());

		// 政治面貌
		poTemp.setPoliticalDic(po.getPoliticalDic());

		// 民族
		poTemp.setNational(po.getNational());

		// 出生日期
		String dateStr = po.getBrithDateStr();
		if (!org.apache.commons.lang.StringUtils.isBlank(dateStr)) {
			poTemp.setBrithDate(DateUtil.toDate(dateStr));
		}

		// 籍贯
		poTemp.setNativeDic(po.getNativeDic());

		// 户口类别
		poTemp.setAddressTypeDic(po.getAddressTypeDic());

		// 户口地址
		poTemp.setNativeAdd(po.getNativeAdd());

		// 家庭地址
		poTemp.setHomeAddress(po.getHomeAddress());

		// 家庭邮政编码
		poTemp.setHomePostCode(po.getHomePostCode());

		// 家庭邮政编码
		poTemp.setHomeTel(po.getHomeTel());

		// 家庭电话
		poTemp.setHomeTel(po.getHomeTel());

		// 手机1
		poTemp.setPhone1(po.getPhone1());

		// 手机2
		poTemp.setPhone2(po.getPhone2());

		// 电子邮箱
		poTemp.setEmail(po.getEmail());

		// QQ
		poTemp.setQq(po.getQq());

		// 网络地址
		poTemp.setUrlStr(po.getUrlStr());

		// 婚姻状况
		poTemp.setMarriageDic(po.getMarriageDic());

		// 港澳台侨
		poTemp.setOverChineseDic(po.getOverChineseDic());

		// 宗教信仰
		poTemp.setReligionDic(po.getReligionDic());

		// 血型
		poTemp.setBloodTypeDic(po.getBloodTypeDic());

		// 健康状况
		poTemp.setHealthStateDic(po.getHealthStateDic());

		// 银行卡号
		poTemp.setBankCode(po.getBankCode());

		// 入党申请
		poTemp.setPartyApp(po.getPartyApp());

		// 入党学习
		poTemp.setPartyStudy(po.getPartyStudy());

		// 备注
		poTemp.setComments(po.getComments());

		// 状态
		// poTemp.setStatus(po.getStatus());

		// 采集状态
		// poTemp.setCollectState(po.getCollectState());

		// 进行数据库更新
		studentInfoDao.update(poTemp);
	}

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
	@Override
	public boolean queryCheckNewStudentPassword(String id, String oldPassWord)
			throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub

		// return studentInfoDao.queryCheckPassword(id, MD5.crypt(oldPassWord));
		return studentInfoDao.queryCheckPassword(id, oldPassWord);
	}

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
	@Override
	public void updateNewStudentPassWord(String id, String newPassWord)
			throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		// studentInfoDao.updatePassWord(id, MD5.crypt(newPassWord));
		studentInfoDao.updatePassWord(id, newPassWord);
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
		// return studentInfoDao.pageQueryStudentReport(pageSize, pageNo, po);
		Page page = studentInfoDao.pageQueryStudentReport(pageSize, pageNo, po);

		if (page != null) {
			ArrayList<StudentInfoModel> list = (ArrayList<StudentInfoModel>) page
					.getResult();
			ArrayList<StudentInfoModel> feeList = new ArrayList<StudentInfoModel>();
			for (StudentInfoModel student : list) {
				// 通过学号调计财处的webservice接口，取得缴费状态
				// feeList.add(getCostState(student));
				// 取得宿舍和缴费状态
				feeList.add(getRoomBystudent(getCostState(student)));
			}

			page.setResult(feeList);
		}

		return page;
	}

	/**
	 * 新生报到办理查询导出
	 * 
	 * @param StudentInfoModel
	 *            学生基本信息Po
	 * 
	 * @return List
	 */
	@Override
	public List<StudentInfoModel> getStudentInfoByReport(StudentInfoModel po) {
		// return studentInfoDao.getStudentInfoByReport(po);

		ArrayList<StudentInfoModel> list = (ArrayList<StudentInfoModel>) studentInfoDao
				.getStudentInfoByReport(po, null);
		ArrayList<StudentInfoModel> feeList = new ArrayList<StudentInfoModel>();

		if (list != null) {
			for (StudentInfoModel student : list) { //
				// 通过学号调计财处的webservice接口，取得缴费状态
				// feeList.add(getCostState(student));getRoomBystudent
				// 取得宿舍
				feeList.add(getRoomBystudent(student));
			}
			return feeList;
		} else {
			return list;
		}

		// return list;
	}

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
	@Override
	public void updateStudentReportStatus(String id, String status,
			Dic siteDic, String cancelReason) {
		// 查询出学生信息
		StudentInfoModel student = this.getStudentInfoById(id);
		if (student != null) {
			// status:状态值
			student.setStatus(status);
			if ("0".equals(status)) {
				// siteDic：报到点
				student.setReportSiteDic(null);
				// 报到时间
				student.setReportDate(null);

			} else if ("1".equals(status)) {
				student.setReportSiteDic(siteDic);
				// 撤销的原因reason：原因
				student.setCancelReason("");
				student.setReportDate(new Date());
			} else if ("2".equals(status)) {
				// siteDic：报到点
				student.setCancelReason(cancelReason);
			} else if ("3".equals(status)) {
				// 取消绿色通道的处理
				// status:状态值
				student.setStatus("1");
				// 撤销的原因reason：原因
				student.setCancelReason("");
			}
			// 更新时间
			student.setUpdateTime(new Date());
			// 进行状态更新
			studentInfoDao.update(student);
		}
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
		// return studentInfoDao.pageQueryGreenWay(pageSize, pageNo, po);
		Page page = studentInfoDao.pageQueryGreenWay(pageSize, pageNo, po);

		if (page != null) {
			ArrayList<StudentInfoModel> list = (ArrayList<StudentInfoModel>) page
					.getResult();
			ArrayList<StudentInfoModel> feeList = new ArrayList<StudentInfoModel>();
			for (StudentInfoModel student : list) { // 通过学号调计财处的webservice接口，取得缴费状态
				// feeList.add(getCostState(student));
				// 取得宿舍
				feeList.add(getRoomBystudent(student));
			}

			page.setResult(feeList);
		}

		return page;
	}

	/**
	 * 
	 * @Title: updateGreenWayStatus
	 * @Description: TODO(对新生绿色通道办理状态的更新)
	 * @param id
	 *            学生Id
	 * @param status
	 *            绿色通道状态
	 * @param reasonDic
	 *            走绿色通道原因
	 * @return void
	 * @author wangcl
	 */
	@Override
	public void updateGreenWayStatus(String id, String status, Dic reasonDic) {
		// 查询出学生信息
		StudentInfoModel student = this.getStudentInfoById(id);
		if (student != null) {
			// value:状态值
			student.setGreenWay(status);
			if ("0".equals(status)) {
				// greenReason：走绿色通道原因
				student.setGreenReason(null);
			} else if ("1".equals(status)) {
				student.setGreenReason(reasonDic);
			}

			// 更新时间
			student.setUpdateTime(new Date());
			// 进行状态更新
			studentInfoDao.update(student);
		}
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
		// return studentInfoDao.pageQueryCancelReport(pageSize, pageNo, po);
		Page page = studentInfoDao.pageQueryCancelReport(pageSize, pageNo, po);

		if (page != null) {
			ArrayList<StudentInfoModel> list = (ArrayList<StudentInfoModel>) page
					.getResult();
			ArrayList<StudentInfoModel> feeList = new ArrayList<StudentInfoModel>();
			for (StudentInfoModel student : list) { 
				// 通过学号调计财处的webservice接口，取得缴费状态
				//feeList.add(getCostState(student));
				// 取得宿舍
				feeList.add(getRoomBystudent(student));
			}

			page.setResult(feeList);
		}

		return page;
	}

	/**
	 * 进行Excel导入学生报到信息
	 * 
	 * @return void
	 * @throws InvocationTargetException
	 */
	@Override
	public String importData(String filePath, String importId, Map initDate,
			Class c) throws ExcelException, InstantiationException,
			IOException, IllegalAccessException, ClassNotFoundException,
			InvocationTargetException {

		// DicUtil dicUtil = DicFactory.getDicUtil();
		// Dic delStatus = dicUtil.getDicInfo("STATUS_NORMAL_DELETED",
		// "NORMAL");
		ImportUtil iu = new ImportUtil();
		// 将Excel数据映射成对象List
		@SuppressWarnings("unchecked")
		List<StudentInfoModel> list = null;
		try {
			list = iu.getDateList(filePath, importId, initDate, c);
		} catch (OfficeXmlFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 错误信息
		String message = "";

		if (list != null && list.size() > 0) {
			// 验证要导入的数据
			message = checkImportData(list);

			if (message == null || "".equals(message)) {

				// 把导入的数据保存到数据库中
				for (StudentInfoModel student : list) {

					// 日期值设置
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					// 出生日期
					/*
					 * Date brithdayUtil = student.getBrithdayUtil(); String
					 * brithdayStr = sdf.format(brithdayUtil);
					 * student.setBrithday(java.sql.Date.valueOf(brithdayStr));
					 */

					// 加入日期
					/*
					 * Date joinDateUtil = student.getJoinDateUtil(); if
					 * (joinDateUtil != null) { String joinDateStr =
					 * sdf.format(joinDateUtil);
					 * student.setJoinDate(joinDateStr); }
					 */

					// 入学日期
					/*
					 * Date entryDateUtil = student.getEntryDateUtil(); String
					 * entryDateStr = sdf.format(entryDateUtil);
					 * student.setEntryDate
					 * (java.sql.Date.valueOf(entryDateStr));
					 */

					// 报到时间
					/*
					 * Date reportDate = student.getReportDate(); String
					 * reportDateStr = sdf.format(reportDate);
					 * student.setReportDate
					 * (java.sql.Date.valueOf(reportDateStr));
					 */
					// 学号
					String number = student.getStuNumber();
					if (number != null && !"".equals(number)) {
						if (this.isExistStudent(number)) {
							// 数据库中已经存在的学生信息
							StudentInfoModel stuUpdate = studentInfoDao
									.getByStudetnNumber(number);
							// 报到状态
							if ("未报到".equals(student.getStatus())) {
								stuUpdate.setStatus("0");
							} else if ("已报到".equals(student.getStatus())) {
								stuUpdate.setStatus("1");
							} else if ("撤销".equals(student.getStatus())) {
								stuUpdate.setStatus("2");
							} else {
								stuUpdate.setStatus("0");
							}

							// 绿色通道
							if ("是".equals(student.getGreenWay())) {
								stuUpdate.setGreenWay("1");
							} else if ("否".equals(student.getGreenWay())) {
								stuUpdate.setGreenWay("0");
							}

							// 绿色通道原因
							if (student.getGreenReason() != null) {
								stuUpdate.setGreenReason(student
										.getGreenReason());
							}

							// 撤销原因
							stuUpdate
									.setCancelReason(student.getCancelReason());

							// 报到时间
							stuUpdate.setReportDate(student.getReportDate());

							// 报到地点
							stuUpdate.setReportSiteDic(student
									.getReportSiteDic());

							// 入学年份
							if (student.getEnterYearDic() != null) {
								stuUpdate.setEnterYearDic(student
										.getEnterYearDic());
							}

							// 更新时间
							stuUpdate.setUpdateTime(new Date());

							this.updateStudentInfo(stuUpdate);
							// this.saveStudent(student);
						}
					}

				}
			}

		} else {
			message = "上传的文件中没有正确的数据！";
		}

		return message;
	}

	/**
	 * 对学籍信息导入的数据进行正确性检查
	 * 
	 * @param List
	 *            导入的数据集合
	 * 
	 * @return String message 错误提示信息
	 */
	private String checkImportData(List<StudentInfoModel> list) {

		StringBuffer returnMessage = new StringBuffer();

		// 数据不为空开始验证
		if (list != null && list.size() > 0) {

			// 验证要导入的数据
			for (int i = 0; i < list.size(); i++) {
				// 验证信息
				StringBuffer message = new StringBuffer();

				StudentInfoModel studentCheck = list.get(i);

				if (studentCheck.getStuNumber() == null
						|| "".equals(studentCheck.getStuNumber().trim())) {
					message.append("学生:" + studentCheck.getName() + " 学号不能为空; ");
				} else {

					if (!this.isExistStudent(studentCheck.getStuNumber())) {
						message.append("学号:" + studentCheck.getStuNumber()
								+ " 的学生系统中不存在;  ");
					}
				}

				/*
				 * //日期验证 SimpleDateFormat sdf = new
				 * SimpleDateFormat("yyyy-MM-dd");
				 * 
				 * //出生日期不能大于当前日期 Date dateNew = new Date(); String dateNewStr =
				 * sdf.format(dateNew);
				 * 
				 * //Date brithdayUtil = studentCheck.getBrithdayUtil(); String
				 * brithdayStr = null; if (brithdayUtil != null) { brithdayStr =
				 * sdf.format(brithdayUtil);
				 * 
				 * if(!DateUtil.compareDate(brithdayStr,dateNewStr)){
				 * message.append("出生日期应小于当前日期; "); } }
				 */
				// 添加错误信息
				if (message != null && message.length() > 0) {
					int index = i + 2;
					returnMessage.append("序号" + index + " "
							+ message.toString() + "<br>");
				}

			}

		}

		return returnMessage.toString();

	}

	/**
	 * 通过学号判断学生是否存在
	 * 
	 * @param stuNumber
	 *            学号
	 * @return boolean
	 */
	@Override
	public boolean isExistStudent(String stuNumber) {
		// 通过学号取得学生信息
		StudentInfoModel sim = studentInfoDao.getByStudetnNumber(stuNumber);
		if (sim != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据学年批量修改缴费状态
	 * 
	 * @param 学年
	 * 
	 * @return boolean
	 */
	@Override
	public boolean updateAllCost(Dic yearDic) {
		StudentInfoModel po = new StudentInfoModel();
		po.setEnterYearDic(yearDic);
		// po.setCostState("0");
		ArrayList<StudentInfoModel> list = (ArrayList<StudentInfoModel>) studentInfoDao
				.getStudentInfoByReport(po, "1");

		if (list != null) {
			int i = 0;
			for (StudentInfoModel student : list) {
				// 通过学号调计财处的webservice接口，取得缴费状态
				// feeList.add(getCostState(student));
				getCostState(student);
				i++;
				// this.updateStudentInfo(student);
				System.out.println(i + "=" + student.getName());
			}
		}

		return true;
	}

	/**
	 * 共通的方法（通过学号调计财处的webservice接口，取得缴费状态）
	 * 
	 * @param stuNumber
	 *            学号
	 * @return boolean
	 */
	private StudentInfoModel getCostState(StudentInfoModel student) {
		if (student != null) {
			String feeStatus;
			try {
				String costState = student.getCostState();
				if (costState == null || "".equals(costState)
						|| "0".equals(costState)) {
					feeStatus = feeServcie.getStudentFeeStatusStr(
							student.getCertificateCode(), "JFZT");

					// 缴清 状态 3
					if (feeStatus != null && !"".equals(feeStatus)) {
						if ("缴清".equals(feeStatus)) {
							// student.setCostState("1");
							studentInfoDao.updateStudentCostState(
									student.getId(), "1");
							student.setCostState("1");
						}
					} else {
						student.setCostState("0");
					}
				} /*
				 * else { student.setCostState("0"); }
				 */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return student;
	}

	/**
	 * 通过学号取得住宿信息
	 * 
	 * @param stuNumber
	 *            学号
	 * @return boolean
	 */
	private StudentInfoModel getRoomBystudent(StudentInfoModel student) {

		if (student != null) {

			StudentRoomModel room = baseDataService.findRoomByStudentId(student
					.getId());
			String roomStr = "";
			if (room != null) {
				if (room.getRoom() != null
						&& room.getRoom().getBuilding() != null) {
					roomStr += room.getBuilding().getName() + "楼 "
							+ room.getRoom().getName() + "寝室 "
							+ room.getBedNumber() + "床位";
				}
				student.setDorm(roomStr);
			}
		}
		return student;

	}
}
