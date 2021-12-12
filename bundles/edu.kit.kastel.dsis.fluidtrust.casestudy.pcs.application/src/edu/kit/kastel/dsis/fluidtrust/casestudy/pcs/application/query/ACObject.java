package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.query;

public enum ACObject implements Named {
    VALUE_DESCRIPTION("ValueDescription"), DESTINATION_KIND_AMOUNT("DestinationKindAmount"), DANGEROUS_GOODS(
            "DangerousGoods"), CONTAINER_ATTRIBUTES(
                    "ContainerAttributes"), VGM("VGM"), DECLARATION("Declaration"), ORDER("Order");

    private final String name;

    private ACObject(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
