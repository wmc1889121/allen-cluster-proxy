package com.example.demo;

import com.allen.common.entity.Command;
import com.allen.common.entity.DataVO;
import com.allen.db.proxy.AllenDBProxy;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private AllenDBProxy allenDBProxy;

    @GetMapping("/{key}")
    public DataVO get(@PathVariable("key") String key) throws Exception {
        return allenDBProxy.get(key);
    }

    @PostMapping
    public Object set(@RequestBody DataVO operate) throws Exception {
        return allenDBProxy.set(operate.getKey(), operate.getVal());
    }
}
