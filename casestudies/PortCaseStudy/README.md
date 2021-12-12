# Port Communication Case Study
* based on [1](https://doi.org/10.5381/jot.2020.19.3.a8)
## General
The folder contains the different palladio models. For viewing or editing please use the [Palladio-Tool](https://www.palladio-simulator.com/tools/download/). Additionaly the folder **image_exports** contains svg images of most important models. It contains the following models:
	* repository, allocation, system and usage model
	* 2 examples from the SEFF-models regarding conditional scenarios
		* only in case of dangrous goods the port authority is notified
## Exceptional cases
### Based on scenario description
* Port Authority can read only dangerous goods, but nothing more

## Structure
The repository diagram shows the available components and their interfaces
![Repository](image_exports/newRepositoryDiagram.svg)
The seffs shows the behaviour of components Here 2 examples can be seen:
![SEFF-Gatein](image_exports/<PortCommunicationSystem><gateIn>SEFFDiagram.svg)
![SEFF-PortOrder](image_exports/<PortCommunicationSystem><portOrder>SEFFDiagram.svg)
The assembly diagram shows the instantiated components in the system
![Assembly](image_exports/newAssemblyDiagram.svg)


### Based on extended scenario description
* Fire
	* During a fire incident the port authority should know the content of all stored containers
	* e.g. avoid mixing of chemicals and chemical reactions or more explosions
* Custom
	* based on some factors the the custom agency decide whether containers need to be physically controlled or whether they just provide the clearance
	* based on the origin country a container might always be controlled e.g. Columbia
* Drug Raid
	* During a drug raid the custom authority needs access to all container data
* Wrong classification of dangerous goods
	* Dangerous good might be wrongly classified
	* then depending on the scenario the port authority might not get the dangerous goods or get too much information
	* might appear at regulation changes, such as in the start port it was labeled as dangerous by the arrival time destination port, it isn't anymore
* Wrong Execution Order
	* e.g. a ship contains undeclared goods
