package uk.ac.cam.sup.forms;

import uk.ac.cam.sup.models.Bin;

import javax.ws.rs.FormParam;

public class BinForm {
    @FormParam("id") private long id;
    @FormParam("owner") private String owner;
    @FormParam("questionSetName") private String questionSetName;
    @FormParam("archived") private Boolean archived;

    public void save(Bin bin) {
        if (owner != null)
            bin.setOwner(owner);

        if (questionSetName != null)
            bin.setQuestionSetName(questionSetName);

        if (archived != null)
            bin.setArchived(archived);
    }
}