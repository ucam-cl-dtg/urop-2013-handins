package uk.ac.cam.sup.forms;

import javax.ws.rs.FormParam;

import org.hibernate.Session;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.models.ProposedQuestion;
import uk.ac.cam.sup.models.UnmarkedSubmission;
import uk.ac.cam.sup.tools.PDFManip;

public class SplittingForm {
    @FormParam("id[]") private long[] questionId;
    @FormParam("startPage[]") private int[] startPage;
    @FormParam("endPage[]") private int[] endPage;
    @FormParam("startLoc[]") private float[] startLoc;
    @FormParam("endLoc[]") private float[] endLoc;

    public boolean validate(UnmarkedSubmission submission) {

        try {
            Session session = HibernateUtil.getSession();

            int pages = (new PDFManip(submission.getFilePath())).getPageCount();

            int elemCount = questionId.length;

            if (elemCount != startPage.length || elemCount != endPage.length || elemCount != startLoc.length || elemCount != endLoc.length)
                return false;

            for (int i = 0; i < elemCount; i++) {
                if (startPage[i] > endPage[i])
                    return false;

                if (startPage[i] == endPage[i] && startLoc[i] <= endLoc[i])
                    return false;

                if (startPage[i] < 1)
                    return false;

                if (endPage[i] > pages)
                    return false;

                if (((ProposedQuestion) session.get(ProposedQuestion.class, questionId[i])).getBin().getId() != submission.getBin().getId())
                    return false;
            }
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }


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
