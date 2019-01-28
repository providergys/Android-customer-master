package com.teaera.teaeracafe.net.Response;

import com.teaera.teaeracafe.net.Model.CategoryInfo;

/**
 * Created by admin on 11/12/2017.
 */

public class PromotedCategoryResponse extends BaseResponse {
    private CategoryInfo category;

    public CategoryInfo getCategory() {
        return category;
    }

    public void setCategory(CategoryInfo category) {
        this.category = category;
    }
}
