
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 学院部门
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/xueyuanbumen")
public class XueyuanbumenController {
    private static final Logger logger = LoggerFactory.getLogger(XueyuanbumenController.class);

    private static final String TABLE_NAME = "xueyuanbumen";

    @Autowired
    private XueyuanbumenService xueyuanbumenService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private CaozuorizhiService caozuorizhiService;

    //级联表非注册的service
    //注册表service
    @Autowired
    private YonghuService yonghuService;
    @Autowired
    private XuexiaoguanliyuanService xuexiaoguanliyuanService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("科研人员".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("学校管理员".equals(role))
            params.put("xuexiaoguanliyuanId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = xueyuanbumenService.queryPage(params);

        //字典表数据转换
        List<XueyuanbumenView> list =(List<XueyuanbumenView>)page.getList();
        for(XueyuanbumenView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"列表查询",list.toString());
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        XueyuanbumenEntity xueyuanbumen = xueyuanbumenService.selectById(id);
        if(xueyuanbumen !=null){
            //entity转view
            XueyuanbumenView view = new XueyuanbumenView();
            BeanUtils.copyProperties( xueyuanbumen , view );//把实体数据重构到view中
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
    caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"单条数据查看",view.toString());
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody XueyuanbumenEntity xueyuanbumen, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,xueyuanbumen:{}",this.getClass().getName(),xueyuanbumen.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<XueyuanbumenEntity> queryWrapper = new EntityWrapper<XueyuanbumenEntity>()
            .eq("xueyuanbumen_name", xueyuanbumen.getXueyuanbumenName())
            .eq("xueyuanbumen_address", xueyuanbumen.getXueyuanbumenAddress())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        XueyuanbumenEntity xueyuanbumenEntity = xueyuanbumenService.selectOne(queryWrapper);
        if(xueyuanbumenEntity==null){
            xueyuanbumen.setCreateTime(new Date());
            xueyuanbumenService.insert(xueyuanbumen);
            caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"新增",xueyuanbumen.toString());
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody XueyuanbumenEntity xueyuanbumen, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,xueyuanbumen:{}",this.getClass().getName(),xueyuanbumen.toString());
        XueyuanbumenEntity oldXueyuanbumenEntity = xueyuanbumenService.selectById(xueyuanbumen.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        //根据字段查询是否有相同数据
        Wrapper<XueyuanbumenEntity> queryWrapper = new EntityWrapper<XueyuanbumenEntity>()
            .notIn("id",xueyuanbumen.getId())
            .andNew()
            .eq("xueyuanbumen_name", xueyuanbumen.getXueyuanbumenName())
            .eq("xueyuanbumen_address", xueyuanbumen.getXueyuanbumenAddress())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        XueyuanbumenEntity xueyuanbumenEntity = xueyuanbumenService.selectOne(queryWrapper);
        if(xueyuanbumenEntity==null){
            xueyuanbumenService.updateById(xueyuanbumen);//根据id更新
            List<String> strings = caozuorizhiService.clazzDiff(xueyuanbumen, oldXueyuanbumenEntity, request,new String[]{"updateTime"});
            caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"修改",strings.toString());
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<XueyuanbumenEntity> oldXueyuanbumenList =xueyuanbumenService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        xueyuanbumenService.deleteBatchIds(Arrays.asList(ids));

        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"删除",oldXueyuanbumenList.toString());
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<XueyuanbumenEntity> xueyuanbumenList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            XueyuanbumenEntity xueyuanbumenEntity = new XueyuanbumenEntity();
//                            xueyuanbumenEntity.setXueyuanbumenUuidNumber(data.get(0));                    //学院部门编号 要改的
//                            xueyuanbumenEntity.setXueyuanbumenName(data.get(0));                    //学院部门名称 要改的
//                            xueyuanbumenEntity.setXueyuanbumenAddress(data.get(0));                    //学院部门位置 要改的
//                            xueyuanbumenEntity.setXueyuanbumenContent("");//详情和图片
//                            xueyuanbumenEntity.setCreateTime(date);//时间
                            xueyuanbumenList.add(xueyuanbumenEntity);


                            //把要查询是否重复的字段放入map中
                                //学院部门编号
                                if(seachFields.containsKey("xueyuanbumenUuidNumber")){
                                    List<String> xueyuanbumenUuidNumber = seachFields.get("xueyuanbumenUuidNumber");
                                    xueyuanbumenUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> xueyuanbumenUuidNumber = new ArrayList<>();
                                    xueyuanbumenUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("xueyuanbumenUuidNumber",xueyuanbumenUuidNumber);
                                }
                        }

                        //查询是否重复
                         //学院部门编号
                        List<XueyuanbumenEntity> xueyuanbumenEntities_xueyuanbumenUuidNumber = xueyuanbumenService.selectList(new EntityWrapper<XueyuanbumenEntity>().in("xueyuanbumen_uuid_number", seachFields.get("xueyuanbumenUuidNumber")));
                        if(xueyuanbumenEntities_xueyuanbumenUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(XueyuanbumenEntity s:xueyuanbumenEntities_xueyuanbumenUuidNumber){
                                repeatFields.add(s.getXueyuanbumenUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [学院部门编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        xueyuanbumenService.insertBatch(xueyuanbumenList);
                        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"批量新增",xueyuanbumenList.toString());
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





}
