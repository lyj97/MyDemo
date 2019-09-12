package Utils.Score;


import android.util.Log;

import com.lu.mydemo.MainActivity;
import com.lu.mydemo.ScoreActivity;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import CJCX.CJCX;
import UIMS.UIMS;

import static com.lu.mydemo.ScoreActivity.loadCJCXScore;

public class ScoreInf {

    static HashMap<Integer, String> index_id = new HashMap<>();
    static HashMap<String, String> courseTypeID_courseType = new HashMap<>();
    static HashMap<String, String> termId_termName = new HashMap<>();//学期ID-NAME

    private static int requiredScoreSum = 0;
    private static double requiredGPASum = 0;
    private static double requiredCreditSum = 0;

    private static int required_custom_ScoreSum = 0;
    private static double required_custom_GPASum = 0;
    private static double required_custom_CreditSum = 0;

    private static boolean bixiu_select = true;
    private static boolean xuanxiu_select = true;
    private static boolean xianxuan_select = true;
    private static boolean xiaoxuanxiu_select = false;
    private static boolean PE_select = false;
    private static boolean chongxiu_select = false;

    static int update_count = -1;

    static boolean scoreListLoaded = false;
//    static int index = 0;//懒加载标识
//    static boolean loadFinished = false;//懒加载完成标识

    static List<Map<String, Object>> dataList;

//    public static List<Map<String, Object>> getFirstPart(){
//        index = 0;
//        if(dataList.size() > 20) {
//            index = 20;
//            return dataList.subList(0, 20);
//        }
//        else {
//            loadFinished = true;
//            return dataList;
//        }
//    }
//
//    public static List<Map<String, Object>> getNextPart(){
//        if(loadFinished) return null;
//        if(dataList.size() > index + 20) {
//            index += 20;
//            return dataList.subList(index - 20, index);
//        }
//        else {
//            loadFinished = true;
//            return dataList.subList(index, dataList.size());
//        }
//    }

    public static void loadScoreList(){
        if(scoreListLoaded) return;
        update_count = 0;
        loadScoreSelect();
        courseTypeID_courseType = UIMS.getCourseTypeId_courseType();
        dataList = new ArrayList<>();
        index_id = new HashMap<>();
        Map<String,Object> map;

        try {
            JSONObject scoreJSON = UIMS.getScoreJSON();
            if(scoreJSON != null) {
                try {

                    JSONArray scores = scoreJSON.getJSONArray("value");

                    double adviceCredit;

                    for (int i = 0; i < scores.size(); i++) {
                        map = new HashMap<>();

                        JSONObject temp = scores.getJSONObject(i);
                        JSONObject teachingTerm = temp.getJSONObject("teachingTerm");
                        JSONObject course = temp.getJSONObject("course");
                        String asId = temp.getString("asId");
                        String courName = course.getString("courName");
                        String termName = teachingTerm.getString("termName");
                        String courScore = temp.getString("score");
                        int scoreNum = temp.getInt("scoreNum");
                        String isReselect = (temp.getString("isReselect").contains("Y")) ? "是" : "否";
                        String gPoint = temp.getString("gpoint");
                        String dateScore = temp.getString("dateScore");
                        String type5 = temp.getString("type5");
                        adviceCredit = course.getDouble("adviceCredit");
                        dateScore = dateScore.replaceAll("T", "  ");

                        map.put("asId", asId);
                        index_id.put(i, asId);
                        map.put("title", courName + "(" + courseTypeID_courseType.get(type5) + ")");
//                map.put("context1","成绩:" + courScore + "  \t  " +
//                        "重修:" + isReselect);
//                map.put("context2",
//                        termName);
                        map.put("context1", termName + "   \t   " +
                                "重修?  " + isReselect);
                        map.put("context2",
                                courScore);
                        map.put("context3",
                                "发布时间： " + dateScore);
                        map.put("context4",
                                adviceCredit);
                        map.put("context5",
                                gPoint);
                        map.put("type", type5);

                        if (!chongxiu_select) {//排除所有重修
                            if (isReselect.equals("否")) {//排除所有重修

//                    if (type5.equals("4161") || type5.equals("4162") || type5.equals("4164") ) {//体育/限选/选修（除校选修）
                                if (xuanxiu_select && type5.equals("4161")) {//选修
                                    required_custom_ScoreSum += scoreNum * adviceCredit;
                                    required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                    required_custom_CreditSum += adviceCredit;
                                }
                                if (xianxuan_select && type5.equals("4162")) {//限选
                                    required_custom_ScoreSum += scoreNum * adviceCredit;
                                    required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                    required_custom_CreditSum += adviceCredit;
                                }
                                if (xiaoxuanxiu_select && type5.equals("4163")) {//校选修
                                    required_custom_ScoreSum += scoreNum * adviceCredit;
                                    required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                    required_custom_CreditSum += adviceCredit;
                                }
                                if (PE_select && type5.equals("4164")) {//体育
                                    required_custom_ScoreSum += scoreNum * adviceCredit;
                                    required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                    required_custom_CreditSum += adviceCredit;
                                }
                                if (type5.equals("4160")) {//仅必修
                                    if (bixiu_select) {
                                        required_custom_ScoreSum += scoreNum * adviceCredit;
                                        required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                        required_custom_CreditSum += adviceCredit;
                                    }
                                    requiredScoreSum += scoreNum * adviceCredit;
                                    requiredGPASum += Double.parseDouble(gPoint) * adviceCredit;
                                    requiredCreditSum += adviceCredit;
                                }

                            }
                        } else {
                            if (type5.equals("4160")) {//仅必修
                                if (bixiu_select) {
                                    required_custom_ScoreSum += scoreNum * adviceCredit;
                                    required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                    required_custom_CreditSum += adviceCredit;
                                }
                                if (isReselect.equals("否")) {//排除重修
                                    requiredScoreSum += scoreNum * adviceCredit;
                                    requiredGPASum += Double.parseDouble(gPoint) * adviceCredit;
                                    requiredCreditSum += adviceCredit;
                                }
                            }
                            if (xuanxiu_select && type5.equals("4161")) {//选修
                                required_custom_ScoreSum += scoreNum * adviceCredit;
                                required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                required_custom_CreditSum += adviceCredit;
                            }
                            if (xianxuan_select && type5.equals("4162")) {//限选
                                required_custom_ScoreSum += scoreNum * adviceCredit;
                                required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                required_custom_CreditSum += adviceCredit;
                            }
                            if (xiaoxuanxiu_select && type5.equals("4163")) {//校选修
                                required_custom_ScoreSum += scoreNum * adviceCredit;
                                required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                required_custom_CreditSum += adviceCredit;
                            }
                            if (PE_select && type5.equals("4164")) {//体育
                                required_custom_ScoreSum += scoreNum * adviceCredit;
                                required_custom_GPASum += Double.parseDouble(gPoint) * adviceCredit;
                                required_custom_CreditSum += adviceCredit;
                            }
                        }

                        dataList.add(map);

                    }
                } catch (Exception e) {
                    Log.e("ScoreInf", "UIMS score load error!");
                    Log.i("ScoreInf", "ScoreJSON:\t" + scoreJSON);
                    e.printStackTrace();
                }
            }

            if(ScoreConfig.isIsCJCXEnable()) {
                try {
                    HashMap<String, org.json.JSONObject> id_JSON = CJCX.getId_JSON();
                    termId_termName = UIMS.getTermId_termName();
                    if (!(termId_termName != null && termId_termName.size() > 0))
                        termId_termName = CJCX.getTermId_termName();
                    if (!ScoreConfig.isIsCJCXEnable() || id_JSON == null) {
                        Log.i("GetScoreList", "IsCJCXEnable:" + ScoreConfig.isIsCJCXEnable() + "\tid_JSON:" + id_JSON);
                        Log.i("GetScoreList", "Ignored CJCX!");
                        Log.i("GetScoreList", "Finished! Size:\t" + dataList.size());
                        return;
                    } else {
                        loadCJCXScore();
                        id_JSON = CJCX.getId_JSON();
                        if (id_JSON == null) {
                            Log.i("GetScoreList", "Ignored CJCX!  Load Failed! No date!");
                            return;
                        }
                    }
                    org.json.JSONObject object;
                    String temp_term;
                    for (String id : id_JSON.keySet()) {
                        if (!index_id.containsValue(id)) {
                            update_count++;
                            try {
                                object = id_JSON.get(id);
                                map = new HashMap<>();
                                map.put("asId", object.getString("lsrId"));
                                map.put("title", object.getString("kcmc"));
                                try {
                                    temp_term = termId_termName.get(object.getString("termId"));
                                    if (temp_term == null) temp_term = "--";
                                    map.put("context1", temp_term + "   \t   " +
                                            "重修?  " + ((object.getString("isReselect").toUpperCase().equals("N")) ? "否" : "是"));
                                } catch (Exception e) {
                                    temp_term = termId_termName.get(object.getString("termId"));
                                    if (temp_term == null) temp_term = "--";
                                    map.put("context1", temp_term + "   \t   " +
                                            "重修?  " + "否");
                                }
                                map.put("context2",
                                        object.getString("cj"));
                                map.put("context3",
                                        "发布时间： " + "--");
                                map.put("context4",
                                        object.getDouble("credit"));
                                map.put("context5",
                                        object.getString("gpoint"));
                                map.put("type", "");
                                dataList.add(0, map);
                            } catch (Exception e) {
                                Log.w("ErrJSON", id_JSON.get(id).toString());
                                e.printStackTrace();
                            }
                        }
                    }

                    if (UIMS.getScoreJSON() == null) {
                        Log.w("ScoreInf", "Sorted score list!!");
                        dataList.sort(new Comparator<Map<String, Object>>() {
                            @Override
                            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                                return ((String) o2.get("context1")).compareTo((String) o1.get("context1"));
                            }
                        });
                    }
                    int index = 0;
                    for (Map<String, Object> temp : dataList) {
                        index_id.put(index++, (String) temp.get("asId"));
                    }
                } catch (Exception e) {
                    Log.e("ScoreInf", "CJCX score load error!");
                    e.printStackTrace();
                }
            }

            scoreListLoaded = true;
            setCountResult();
            Log.i("GetScoreList", "Finished! Size:\t" + dataList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadScoreSelect() {
        xuanxiu_select = ScoreActivity.isXuanxiu_select();
        xianxuan_select = (ScoreActivity.isXianxuan_select());
        xiaoxuanxiu_select = (ScoreActivity.isXiaoxuanxiu_select());
        PE_select = ScoreActivity.isPE_select();
    }

    private static void setCountResult(){
        ScoreActivity.setRequiredScoreSum(requiredScoreSum);
        ScoreActivity.setRequiredGPASum(requiredGPASum);
        ScoreActivity.setRequiredCreditSum(requiredCreditSum);
        ScoreActivity.setRequired_custom_ScoreSum(required_custom_ScoreSum);
        ScoreActivity.setRequired_custom_GPASum(required_custom_GPASum);
        ScoreActivity.setRequired_custom_CreditSum(required_custom_CreditSum);
        ScoreActivity.setIndex_id(index_id);
    }

    public static int getRequiredScoreSum() {
        return requiredScoreSum;
    }

    public static void setRequiredScoreSum(int requiredScoreSum) {
        ScoreInf.requiredScoreSum = requiredScoreSum;
    }

    public static double getRequiredGPASum() {
        return requiredGPASum;
    }

    public static void setRequiredGPASum(double requiredGPASum) {
        ScoreInf.requiredGPASum = requiredGPASum;
    }

    public static double getRequiredCreditSum() {
        return requiredCreditSum;
    }

    public static void setRequiredCreditSum(double requiredCreditSum) {
        ScoreInf.requiredCreditSum = requiredCreditSum;
    }

    public static int getRequired_custom_ScoreSum() {
        return required_custom_ScoreSum;
    }

    public static void setRequired_custom_ScoreSum(int required_custom_ScoreSum) {
        ScoreInf.required_custom_ScoreSum = required_custom_ScoreSum;
    }

    public static double getRequired_custom_GPASum() {
        return required_custom_GPASum;
    }

    public static void setRequired_custom_GPASum(double required_custom_GPASum) {
        ScoreInf.required_custom_GPASum = required_custom_GPASum;
    }

    public static double getRequired_custom_CreditSum() {
        return required_custom_CreditSum;
    }

    public static void setRequired_custom_CreditSum(double required_custom_CreditSum) {
        ScoreInf.required_custom_CreditSum = required_custom_CreditSum;
    }

    public static boolean isBixiu_select() {
        return bixiu_select;
    }

    public static void setBixiu_select(boolean bixiu_select) {
        ScoreInf.bixiu_select = bixiu_select;
    }

    public static boolean isXuanxiu_select() {
        return xuanxiu_select;
    }

    public static void setXuanxiu_select(boolean xuanxiu_select) {
        ScoreInf.xuanxiu_select = xuanxiu_select;
    }

    public static boolean isXianxuan_select() {
        return xianxuan_select;
    }

    public static void setXianxuan_select(boolean xianxuan_select) {
        ScoreInf.xianxuan_select = xianxuan_select;
    }

    public static boolean isXiaoxuanxiu_select() {
        return xiaoxuanxiu_select;
    }

    public static void setXiaoxuanxiu_select(boolean xiaoxuanxiu_select) {
        ScoreInf.xiaoxuanxiu_select = xiaoxuanxiu_select;
    }

    public static boolean isPE_select() {
        return PE_select;
    }

    public static void setPE_select(boolean PE_select) {
        ScoreInf.PE_select = PE_select;
    }

    public static boolean isChongxiu_select() {
        return chongxiu_select;
    }

    public static void setChongxiu_select(boolean chongxiu_select) {
        ScoreInf.chongxiu_select = chongxiu_select;
    }

    public static boolean isScoreListLoaded() {
        return scoreListLoaded;
    }

    public static void setScoreListLoaded(boolean scoreListLoaded) {
        ScoreInf.scoreListLoaded = scoreListLoaded;
    }

//    public static boolean isLoadFinished() {
//        return loadFinished;
//    }
//
//    public static void setLoadFinished(boolean loadFinished) {
//        ScoreInf.loadFinished = loadFinished;
//    }

    public static List<Map<String, Object>> getDataList() {
        return dataList;
    }

    public static int getUpdate_count() {
        return update_count;
    }
}
