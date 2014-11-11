package leadsm24.rest;

import com.google.common.collect.ImmutableSet;
import restx.factory.*;
import leadsm24.rest.LEADSM24ResourceRouter;

@Machine
public class LEADSM24ResourceRouterFactoryMachine extends SingleNameFactoryMachine<LEADSM24ResourceRouter> {
    public static final Name<LEADSM24ResourceRouter> NAME = Name.of(LEADSM24ResourceRouter.class, "LEADSM24ResourceRouter");

    public LEADSM24ResourceRouterFactoryMachine() {
        super(0, new StdMachineEngine<LEADSM24ResourceRouter>(NAME, 0, BoundlessComponentBox.FACTORY) {
private final Factory.Query<leadsm24.rest.LEADSM24Resource> resource = Factory.Query.byClass(leadsm24.rest.LEADSM24Resource.class).mandatory();
private final Factory.Query<restx.entity.EntityRequestBodyReaderRegistry> readerRegistry = Factory.Query.byClass(restx.entity.EntityRequestBodyReaderRegistry.class).mandatory();
private final Factory.Query<restx.entity.EntityResponseWriterRegistry> writerRegistry = Factory.Query.byClass(restx.entity.EntityResponseWriterRegistry.class).mandatory();
private final Factory.Query<restx.converters.MainStringConverter> converter = Factory.Query.byClass(restx.converters.MainStringConverter.class).mandatory();
private final Factory.Query<javax.validation.Validator> validator = Factory.Query.byClass(javax.validation.Validator.class).mandatory();
private final Factory.Query<restx.security.RestxSecurityManager> securityManager = Factory.Query.byClass(restx.security.RestxSecurityManager.class).mandatory();

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(
resource,
readerRegistry,
writerRegistry,
converter,
validator,
securityManager
                ));
            }

            @Override
            protected LEADSM24ResourceRouter doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new LEADSM24ResourceRouter(
satisfiedBOM.getOne(resource).get().getComponent(),
satisfiedBOM.getOne(readerRegistry).get().getComponent(),
satisfiedBOM.getOne(writerRegistry).get().getComponent(),
satisfiedBOM.getOne(converter).get().getComponent(),
satisfiedBOM.getOne(validator).get().getComponent(),
satisfiedBOM.getOne(securityManager).get().getComponent()
                );
            }
        });
    }

}
