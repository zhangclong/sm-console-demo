package com.uh.console.domain;

import com.uh.common.core.domain.BaseEntity;

public class VersionImages extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long versionId;

    private String pullSecrets;

    private String centerImage;

    private String nodeImage;

    private String proxyImage;

    private String exporterImage;

    public VersionImages() {
    }

    public VersionImages(String centerImage, String nodeImage, String proxyImage, String exporterImage) {
        this.centerImage = centerImage;
        this.nodeImage = nodeImage;
        this.proxyImage = proxyImage;
        this.exporterImage = exporterImage;
    }


    /**
     * 检查image中有值的image是否包含在当前对象的image中
     * @param image
     * @return
     */
    public boolean includes(VersionImages image) {
        return includesString(this.pullSecrets, image.getPullSecrets()) &&
                includesString(this.centerImage, image.getCenterImage()) &&
                includesString(this.nodeImage, image.getNodeImage()) &&
                includesString(this.proxyImage, image.getProxyImage()) &&
                includesString(this.exporterImage, image.getExporterImage());
    }

    /**
     * 对比包括null的字符串是否相等。
     * 如果同为null或者s2为null, 返回true, 否则返回base.equals(s2)
     *
     * @param base
     * @param s2
     * @return
     */
    private boolean includesString(String base, String s2) {
        String baseStr = (base != null) ? base.trim() : null;
        String s2Str = (s2 != null) ? s2.trim() : null;

        if(baseStr == null && s2Str == null) {
            return true;
        }

        return baseStr != null && (s2Str == null || baseStr.equals(s2Str));
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getCenterImage() {
        return centerImage;
    }

    public void setCenterImage(String centerImage) {
        this.centerImage = centerImage;
    }

    public String getNodeImage() {
        return nodeImage;
    }

    public void setNodeImage(String nodeImage) {
        this.nodeImage = nodeImage;
    }

    public String getProxyImage() {
        return proxyImage;
    }

    public void setProxyImage(String proxyImage) {
        this.proxyImage = proxyImage;
    }

    public String getExporterImage() {
        return exporterImage;
    }

    public void setExporterImage(String exporterImage) {
        this.exporterImage = exporterImage;
    }

    public String getPullSecrets() {
        return pullSecrets;
    }

    public void setPullSecrets(String pullSecrets) {
        this.pullSecrets = pullSecrets;
    }

    public static void main(String[] args) {
        //测试 imagesSkipNullEquals
        VersionImages image1 = new VersionImages();
        image1.setPullSecrets("secret1");
        image1.setCenterImage("center1");
        image1.setNodeImage("node1");
        image1.setProxyImage("proxy1");
        image1.setExporterImage("exporter1");

        VersionImages image2 = new VersionImages();
        image2.setPullSecrets("secret1");
        image2.setCenterImage("center1");
        System.out.println(image1.includes(image2)); //true


        VersionImages image3 = new VersionImages();
        image3.setPullSecrets("secret1");
        image3.setNodeImage("node3");
        System.out.println(image1.includes(image3)); //false


        VersionImages image4 = new VersionImages();
        image4.setPullSecrets("secret1");
        image4.setNodeImage("node1");
        image4.setProxyImage("proxy1");
        System.out.println(image1.includes(image4)); //true


        VersionImages image5 = new VersionImages();
        image5.setPullSecrets("secret1");
        image5.setNodeImage("node1");
        image5.setProxyImage("proxy1");
        image5.setExporterImage("exporter1");
        System.out.println(image1.includes(image5)); //true


        VersionImages imageB1 = new VersionImages();
        imageB1.setPullSecrets(null);
        imageB1.setCenterImage("center1");
        imageB1.setNodeImage("node1");
        imageB1.setProxyImage("proxy1");
        imageB1.setExporterImage(null);

        VersionImages imageB2 = new VersionImages();
        imageB2.setCenterImage("center1");
        System.out.println(imageB1.includes(imageB2)); //true


        VersionImages imageB3 = new VersionImages();
        imageB3.setNodeImage("node3");
        System.out.println(imageB1.includes(imageB3)); //false


        VersionImages imageB4 = new VersionImages();
        imageB4.setNodeImage("node1");
        imageB4.setProxyImage("proxy1");
        System.out.println(imageB1.includes(imageB4)); //true


        VersionImages imageB5 = new VersionImages();
        imageB5.setNodeImage("node1");
        imageB5.setProxyImage("proxy12");
        System.out.println(imageB1.includes(image5)); //false


    }
}
