package com.eat.reggie.dto;

import com.eat.reggie.entity.Setmeal;
import com.eat.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
