package com.uws.orientation.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import com.uws.core.excel.ExcelException;
import com.uws.core.excel.rule.IRule;
import com.uws.core.excel.vo.ExcelColumn;
import com.uws.core.excel.vo.ExcelData;
import com.uws.core.util.SpringBeanLocator;
import com.uws.orientation.service.IStudentInfoService;
import com.uws.sys.model.Dic;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.impl.DicFactory;

/**
 * 
 * @ClassName: StudentRule
 * @Description: TODO(对新生报到导入信息的验证)
 * @author wangcl
 * @date 2015-08-07 14:04:48
 * 
 */
public class StudentRule implements IRule {

	@Autowired
	private DicUtil dicUtil = DicFactory.getDicUtil();

	private String getString(int site, Map<String, ExcelData> eds, String key) {
		String s = "";
		String keyName = "$" + key + "$" + site;
		if ((eds.get(keyName) != null)
				&& (((ExcelData) eds.get(keyName)).getValue() != null)) {
			s = s + (String) ((ExcelData) eds.get(keyName)).getValue();
		}
		return s.trim();
	}

	@Override
	public void format(ExcelData arg0, ExcelColumn arg1, Map arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void operation(ExcelData data, ExcelColumn column, Map arg2,
			Map<String, ExcelData> eds, int site) {
		// TODO Auto-generated method stub
		// 报到地点
		if ("reportSiteDic".equals(column.getName())) {
			List<Dic> dicReportSite = this.dicUtil
					.getDicInfoList("REPORT_SITE");
			String reportSiteValue = getString(site, eds, "P");
			for (Dic dic : dicReportSite)
				if (reportSiteValue.equals(dic.getName())) {
					data.setValue(dic);
					break;
				}
		}
		// 性别
		if ("genderDic".equals(column.getName())) {
			List<Dic> dicGendere = this.dicUtil.getDicInfoList("GENDER");
			String genderValue = getString(site, eds, "I");
			for (Dic dic : dicGendere)
				if (genderValue.equals(dic.getName())) {
					data.setValue(dic);
					break;
				}
		}

		// 绿色通道的原因
		if ("greenReason".equals(column.getName())) {
			List<Dic> dicGreenReason = this.dicUtil
					.getDicInfoList("GREEN_WAY_REASON");
			String greenReasonValue = getString(site, eds, "L");
			for (Dic dic : dicGreenReason)
				if (greenReasonValue.equals(dic.getName())) {
					data.setValue(dic);
					break;
				}
		}

		// 入学年份
		if ("enterYearDic".equals(column.getName())) {
			List<Dic> dicYear = this.dicUtil
					.getDicInfoList("YEAR");
			String yearValue = getString(site, eds, "N");
			for (Dic dic : dicYear)
				if (yearValue.equals(dic.getCode())) {
					data.setValue(dic);
					break;
				}
		}

	}

	@Override
	public void validate(ExcelData arg0, ExcelColumn column, Map arg2)
			throws ExcelException {
		// TODO Auto-generated method stub
		IStudentInfoService service = (IStudentInfoService) SpringBeanLocator
				.getBean("com.uws.orientation.service.impl.StudentInfoServiceImpl");
		if ("学号".equals(column.getName())) {
			String code = "";
			boolean flag = false;
			code = arg0.getValue().toString();

			flag = service.isExistStudent(code);

			if (!flag) {
				String isText = arg0.getId().replaceAll("\\$", "");
				throw new ExcelException(isText + "单元格值("
						+ arg0.getValue().toString()
						+ ")与在系统中没有找到匹配的学号，请修正后重新上传；<br/>");
			}
		}

	}
}
