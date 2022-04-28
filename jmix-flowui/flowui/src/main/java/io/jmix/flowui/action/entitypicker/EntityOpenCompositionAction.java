package io.jmix.flowui.action.entitypicker;


import io.jmix.flowui.action.ActionType;

@ActionType(EntityOpenCompositionAction.ID)
public class EntityOpenCompositionAction<E> extends EntityOpenAction<E> {

    public static final String ID = "entity_openComposition";

    public EntityOpenCompositionAction() {
        super(ID);
    }

    public EntityOpenCompositionAction(String id) {
        super(id);
    }

    @Override
    protected boolean isEmpty() {
        return false;
    }
}
