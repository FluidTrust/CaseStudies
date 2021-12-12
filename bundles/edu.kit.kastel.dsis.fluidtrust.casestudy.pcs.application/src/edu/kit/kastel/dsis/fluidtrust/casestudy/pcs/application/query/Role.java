package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.query;

public enum Role implements Named {
    EXPORTER("Exporter"), CUSTOMS("Customs"), SHIPPING_LINE("ShippingLine"), PCS("PCS"), TERMINAL(
            "Terminal"), PORT_AUTHORITY("PortAuthority"), ALL("ALL");

    private final String name;

    private Role(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
