package ${project_rootPackage}.screen.foo;

import ${project_rootPackage}.entity.Foo;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;

@UiController("${project_id}_Foo.browse")
@UiDescriptor("foo-browse.xml")
@LookupComponent("foosTable")
@Route("foos")
public class FooBrowse extends StandardLookup<Foo> {
}