package test_support.entity.constraints;

import test_support.entity.Car;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PurchasedAfterManufacturedValidator implements ConstraintValidator<PurchasedAfterManufactured, Car> {

    public void initialize(PurchasedAfterManufactured constraint) {
        // do nothing
    }

    public boolean isValid(Car car, ConstraintValidatorContext context) {
//        EntityStates entityStates = AppBeans.get(EntityStates.class);
//
//        if (!entityStates.isLoaded(car, "manufactureDate")
//                || !entityStates.isLoaded(car, "purchaseDate")
//                || car.getManufactureDate() == null
//                || car.getPurchaseDate() == null) {
            return true;
//        } else {
//            return car.getManufactureDate().before(car.getPurchaseDate());
//        }
    }
}
