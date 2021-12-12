package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.query

final class AccessControlPolicy {

	public static val POLICY = #{
		Role.EXPORTER ->
			#[ACObject.VALUE_DESCRIPTION, ACObject.DESTINATION_KIND_AMOUNT, ACObject.DANGEROUS_GOODS,
				ACObject.CONTAINER_ATTRIBUTES, ACObject.VGM, ACObject.DECLARATION, ACObject.ORDER],
		Role.CUSTOMS ->
			#[ACObject.VALUE_DESCRIPTION, ACObject.DESTINATION_KIND_AMOUNT, ACObject.DANGEROUS_GOODS,
				ACObject.DECLARATION, ACObject.ORDER],
		Role.SHIPPING_LINE ->
			#[ACObject.DESTINATION_KIND_AMOUNT, ACObject.DANGEROUS_GOODS, ACObject.CONTAINER_ATTRIBUTES, ACObject.VGM,
				ACObject.DECLARATION, ACObject.ORDER],
		Role.PCS ->
			#[ACObject.DESTINATION_KIND_AMOUNT, ACObject.DANGEROUS_GOODS, ACObject.CONTAINER_ATTRIBUTES, ACObject.VGM,
				ACObject.DECLARATION, ACObject.ORDER],
		Role.TERMINAL ->
			#[ACObject.DESTINATION_KIND_AMOUNT, ACObject.DANGEROUS_GOODS, ACObject.CONTAINER_ATTRIBUTES, ACObject.VGM,
				ACObject.DECLARATION, ACObject.ORDER],
		Role.PORT_AUTHORITY -> #[ACObject.DANGEROUS_GOODS, ACObject.DECLARATION, ACObject.ORDER],
		Role.ALL -> ACObject.values.toList
	}

	private new() {
		// intentionally left blank
	}

}
