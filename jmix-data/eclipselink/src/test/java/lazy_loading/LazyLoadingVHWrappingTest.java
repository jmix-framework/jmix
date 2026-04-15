/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lazy_loading;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.SaveContext;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.DataTestConfiguration;
import test_support.TestContextInititalizer;
import test_support.entity.lazyloading.instantiated_vh_wrapping.InfoEntity;
import test_support.entity.lazyloading.instantiated_vh_wrapping.LastEntity;
import test_support.entity.lazyloading.instantiated_vh_wrapping.MyEntity;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, DataTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
public class LazyLoadingVHWrappingTest {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected JdbcTemplate jdbc;


    @BeforeEach
    public void setUp() {
        MyEntity myEntity = dataManager.create(MyEntity.class);
        LastEntity lastEntity = dataManager.create(LastEntity.class);
        LastEntity secondLastEntity = dataManager.create(LastEntity.class);
        InfoEntity infoEntity = dataManager.create(InfoEntity.class);
        InfoEntity secondInfoEntity = dataManager.create(InfoEntity.class);

        myEntity.getMetaEntity().setLastentity(lastEntity);

        myEntity.setInfoEntity(secondInfoEntity);
        lastEntity.setInfoEntity(infoEntity);

        final SaveContext saveContext = new SaveContext();
        saveContext.getEntitiesToSave().add(myEntity);
        saveContext.getEntitiesToSave().add(lastEntity);
        saveContext.getEntitiesToSave().add(secondInfoEntity);
        saveContext.getEntitiesToSave().add(infoEntity);

        dataManager.save(saveContext);
    }

    @Test
    public void deepNestedUOWSingleValueHolderWrappingTest() {


        List<MyEntity> entites = dataManager.load(MyEntity.class).all().fetchPlan(FetchPlan.BASE).list();

        MyEntity loadedOne = entites.get(0);

        var metaEntity = loadedOne.getMetaEntity();
        var loadedLastEntity = metaEntity.getLastentity();
        var loadedInfoEntity = loadedLastEntity.getInfoEntity();


        Assertions.assertNotNull(metaEntity);
        Assertions.assertNotNull(loadedOne);
        Assertions.assertNotNull(loadedInfoEntity);
    }

    @AfterEach
    public void tearDown() {
        jdbc.update("delete from TST_VH_MY_ENTITY");
        jdbc.update("delete from TST_VH_LAST_ENTITY");
        jdbc.update("delete from TST_VH_INFO_ENTITY");
    }
}
