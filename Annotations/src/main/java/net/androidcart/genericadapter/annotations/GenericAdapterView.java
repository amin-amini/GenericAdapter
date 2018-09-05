package net.androidcart.genericadapter.annotations;

/**
 * Created by Amin Amini on 9/5/18.
 */

public interface GenericAdapterView<Model> {
    void onBind(Model model);
}
