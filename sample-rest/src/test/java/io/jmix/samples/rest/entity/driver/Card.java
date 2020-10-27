/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.samples.rest.entity.driver;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.samples.rest.entity.StandardEntity;
import io.jmix.samples.rest.entity.sec.User;

import javax.persistence.*;
import java.util.Set;

@Entity(name = "ref$Card")
@JmixEntity
@Table(name = "REF_CARD")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "CARD_TYPE", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
@SystemLevel
public class Card extends StandardEntity {

    private static final long serialVersionUID = -6180254942462308853L;

    @Column(name = "STATE", length = 255)
    protected String state;

    @InstanceName
    @Column(name = "DESCRIPTION", length = 1000)
    protected String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATOR_ID")
    protected User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBSTITUTED_CREATOR_ID")
    protected User substitutedCreator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_CARD_ID")
    @OnDeleteInverse(DeletePolicy.DENY)
    protected Card parentCard;

    @OneToMany(mappedBy = "parentCard")
    protected Set<Card> subCards;

    @Column(name = "HAS_ATTACHMENTS")
    protected Boolean hasAttachments = false;

    @Column(name = "HAS_ATTRIBUTES")
    protected Boolean hasAttributes = false;

    @Column(name = "PARENT_CARD_ACCESS")
    protected Boolean parentCardAccess = false;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Card getParentCard() {
        return parentCard;
    }

    public void setParentCard(Card parentCard) {
        this.parentCard = parentCard;
    }

    public Set<Card> getSubCards() {
        return subCards;
    }

    public void setSubCards(Set<Card> subCards) {
        this.subCards = subCards;
    }

    public User getSubstitutedCreator() {
        return substitutedCreator;
    }

    public void setSubstitutedCreator(User substitutedCreator) {
        this.substitutedCreator = substitutedCreator;
    }

    public Boolean getHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(Boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    public Boolean getHasAttributes() {
        return hasAttributes;
    }

    public void setHasAttributes(Boolean hasAttributes) {
        this.hasAttributes = hasAttributes;
    }

    public Boolean getParentCardAccess() {
        return parentCardAccess;
    }

    public void setParentCardAccess(Boolean parentCardAccess) {
        this.parentCardAccess = parentCardAccess;
    }
}
