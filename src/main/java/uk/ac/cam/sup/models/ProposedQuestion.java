package uk.ac.cam.sup.models;

// TODO: KILL ME!

import com.google.common.collect.ImmutableMap;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "ProposedQuestion")
public class ProposedQuestion {
    // Fields
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="proposedQuestionIdSeq")
    @SequenceGenerator(name="proposedQuestionIdSeq", sequenceName="ProposedQuestionSEQ", allocationSize=1)
    private long id;

    private Date dateCreated;

    private String name;
    private String link;

    @ManyToOne
    private Bin bin;

    @OneToMany(mappedBy = "question")
    private Set<Answer> answers;

    // Constructors
    public ProposedQuestion() {
        dateCreated = new Date();
    }

    // Id
    public long getId() {
        return id;
    }

    // Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Bin
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    // Answers
    public Set<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set<Answer> answers) {
        this.answers = answers;
    }

    // DateCreated
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    // Link
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ImmutableMap<String,Object> toJSON() {
        ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();

        builder.put("id", getId())
               .put("name", getName())
               .put("bin", getBin().getId());

        if (link != null)
            builder.put("link", getLink());

        return builder.build();
    }
}
