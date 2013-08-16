package uk.ac.cam.sup.forms;

import uk.ac.cam.sup.models.Bin;

import javax.ws.rs.FormParam;

public class BinForm {
    @FormParam("id") private long id;
    @FormParam("owner") private String owner;
    @FormParam("questionSetName") private String questionSetName;
    @FormParam("isArchived") private boolean isArchived;

    public void save(Bin bin) {
        bin.setQuestionSetName(questionSetName);
        bin.setArchived(isArchived);
        bin.setOwner(owner);
    }
}