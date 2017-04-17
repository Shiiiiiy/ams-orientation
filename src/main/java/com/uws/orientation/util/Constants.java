package com.uws.orientation.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 迎新管理所使用的常量信息
 * 
 * @author wangchenglong
 * @date 2015-07-08
 */
public class Constants {
	
	/** 迎新数据设置信息修改 **/
	public final static String WELCOME_SET="/welcome/set";
	
	/** 迎新数据设置信息修改页面 **/
	public final static String WELCOME_SET_FTL="orientation/scene/dataset/";
	
	/**迎新现场报到  **/
	public final static String SCENE_REPORT="/scene/report";
	
	/** 迎新现场报到页面 **/
	public final static String SCENE_REPORT_FTL="orientation/scene/report/";
	
	/**绿色通道办理  **/
	public final static String GREEN_WAY="/green/way";
	
	/** 绿色通道办理页面 **/
	public final static String GREEN_WAY_FTL="orientation/scene/greenway/";
	
	/**撤销报到办理  **/
	public final static String CANCEL_REPORT="/cancel/report";
	
	/** 撤销报到办理页面 **/
	public final static String CANCEL_REPORT_FTL="orientation/scene/cancelreport/";
	
	/** url中返回的标识 **/
	public static final String BACK_FLAG="backFlag";
	
	/** 报到的学生ID **/
	public static final String REPORT_STU_ID="reportStuId";
	
	
	/**
	 * 监管分析页面（预期报到查询）
	 */
	public final static String REGULATORY_ANALYSIS = "orientation/regulatoryAnalysis";
	
	/**
	 * 监管分析页面(预期报到统计)
	 */
	public final static String EX_REPORT_COUNT = "orientation/reportCount";
	
	/**
	 * 监管分析页面(报到进度统计)
	 */
	public final static String REPORT_PROGRESS = "orientation/reportProgress";
	
	/**
	 * 监管分析页面(各报到点统计)
	 */
	public final static String REPORT_PLACE = "orientation/reportPlace";
	
	/**
	 * 监管分析页面(绿色通道统计)
	 */
	public final static String GREEN_CHANNEL = "orientation/greenChannel";
	
	/**
	 * 监管分析页面(院系缴费统计)
	 */
	public final static String REPORT_PAYMENT = "orientation/countPayment";
	
	/**
	 * 监管分析页面(分时统计)
	 */
	public final static String TIME_COUNT = "orientation/timeCount";
	
	/**
	 * 监管分析页面(分时统计)
	 */
	public final static String LIVE_COUNT = "orientation/liveCount";
	
	/**
	 * 对于是否(需要)的字段，需要(是)
	 */
	public final static String REGULATORY_NEED = "1";
	
	/**
	 * 对于是否(需要)的字段，不需要(否)
	 */
	public final static String REGULATORY_NEED_NOT = "0";
	
	/** 报到状态 **/
	public final static List<String[]> SCENE_REPORT_STATE_LIST = getInstallSceneReportState();
	public static List<String[]> getInstallSceneReportState(){
		List<String[]> list = new ArrayList<String[]>();
		list.add(new String[]{"0","未报到"});
		list.add(new String[]{"1","已报到"});
		list.add(new String[]{"2","已撤销"});
		return list;
	}
	
	/** 绿色通道的状态 **/
	public final static List<String[]> GREEN_CHANNEL_STATE_LIST = getInstallGreenChannelState();
	public static List<String[]> getInstallGreenChannelState(){
		List<String[]> list = new ArrayList<String[]>();
		list.add(new String[]{"1","是"});
		list.add(new String[]{"0","否"});
		return list;
	}
	
	/** 撤销报到的状态 **/
	public final static List<String[]> CANCEL_REPORT_STATE_LIST = getInstallCancelReportState();
	public static List<String[]> getInstallCancelReportState(){
		List<String[]> list = new ArrayList<String[]>();
		list.add(new String[]{"1","已报到"});
		list.add(new String[]{"2","已撤销"});
		return list;
	}
}