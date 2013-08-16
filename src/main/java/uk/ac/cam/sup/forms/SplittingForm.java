package uk.ac.cam.sup.forms;

import javax.ws.rs.FormParam;

public class SplittingForm {
    @FormParam("id[]") private long[] questionId;
    @FormParam("startPage[]") private int[] startPage;
    @FormParam("endPage[]") private int[] endPage;
    @FormParam("startLoc[]") private float[] startLoc;
    @FormParam("endLoc[]") private float[] endLoc;

    public int elements() {
        return questionId.length;
    }

    public long getQuestionId(int index) {
        return questionId[index];
    }

    public int getStartPage(int index) {
        return startPage[index];
    }

    public int getEndPage(int index) {
        return endPage[index];
    }

    public float getStartLoc(int index) {
        return startLoc[index];
    }

    public float getEndLoc(int index) {
        return endLoc[index];
    }
}
