package fr.cm.scorexpress.data.node;

public class NodeCategories extends NodeFactory {

    public Object accept(final IManifVisitor visitor, final Object data) {
        return visitor.visiteCategories(this, data);
    }

}
