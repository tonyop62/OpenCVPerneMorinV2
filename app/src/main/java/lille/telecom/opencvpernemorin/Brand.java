package lille.telecom.opencvpernemorin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by E34575 on 25/11/2015.
 */
public class Brand {

    String brandName;
    String url;
    String classifier;
    List<String> images;

    public Brand() {
        this.images = new ArrayList<>();
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

}
