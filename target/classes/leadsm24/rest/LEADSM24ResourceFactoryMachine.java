package leadsm24.rest;

import com.google.common.collect.ImmutableSet;
import restx.factory.*;
import leadsm24.rest.LEADSM24Resource;

@Machine
public class LEADSM24ResourceFactoryMachine extends SingleNameFactoryMachine<LEADSM24Resource> {
    public static final Name<LEADSM24Resource> NAME = Name.of(LEADSM24Resource.class, "LEADSM24Resource");

    public LEADSM24ResourceFactoryMachine() {
        super(0, new StdMachineEngine<LEADSM24Resource>(NAME, 0, BoundlessComponentBox.FACTORY) {


            @Override
            public BillOfMaterials getBillOfMaterial() {
                return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(

                ));
            }

            @Override
            protected LEADSM24Resource doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new LEADSM24Resource(

                );
            }
        });
    }

}
