package ink.rainbowbridge.arathoth.Attributes.SubAttributes;

import ink.rainbowbridge.arathoth.API.ArathothAPI;
import ink.rainbowbridge.arathoth.Arathoth;
import ink.rainbowbridge.arathoth.Attributes.NumberAttribute;
import ink.rainbowbridge.arathoth.Attributes.data.AttributeData;
import ink.rainbowbridge.arathoth.Events.ArathothAttackCDEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 牛逼攻速抑制，通过监听AttackCD事件实现
 * 同时也是监听Arathoth事件操作属性的实例
 *
 * @author 寒雨
 * @create 2020/12/27 0:11
 */
public class AttackSpeedInhabit implements NumberAttribute, Listener {
    private FileConfiguration config;
    private Pattern Primary;
    private Pattern Regular;
    private Pattern Percent;
    private boolean isEnable;

    @Override
    public AttributeData parseNumber(List<String> uncoloredlores) {
        /*
         * 0.1.3 时代新parse方法
         */
        AttributeData value = new AttributeData();
        for (String str : uncoloredlores) {
            Matcher m1 = Primary.matcher(str);
            Matcher m2 = Regular.matcher(str);
            Matcher m3 = Percent.matcher(str);
            if(m1.find()){
                value.setPrimary(value.getPrimary() + Double.valueOf(m1.group(1)));
                value.setRegular(value.getRegular() + Double.valueOf(m1.group(1)));
            }
            if (m2.find()){
                value.setPrimary(value.getPrimary() + Double.valueOf(m2.group(1)));
                value.setRegular(value.getRegular() + Double.valueOf(m2.group(6)));
            }
            if (m3.find()){
                value.setPercent(value.getPercent() + Double.valueOf(m3.group(1)));
            }
        }
        return value;
    }

    @Override
    public void function(Event e) {
        //TODO 空壳属性无需function
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void register(Plugin plugin) {
        // TODO 载入配置
        if(ArathothAPI.AttributeConfigDefaultSet(this)){
            config = ArathothAPI.getAttributeConfig(this);
            config.set(getName()+".Enable", true);
            config.set(getName()+".Pattern", getName()+": [VALUE]");
            ArathothAPI.saveAttributeConfig(this,config);
        }
        else{
            config = ArathothAPI.getAttributeConfig(this);
        }
        Bukkit.getPluginManager().registerEvents(this,Arathoth.getInstance());

        Primary = Pattern.compile(config.getString(getName()+".Pattern").replace("[VALUE]", "((\\-|\\+)?(\\d+(\\.\\d+)?))"));
        Regular = Pattern.compile(config.getString(getName()+".Pattern").replace("[VALUE]", "((\\-|\\+)?(\\d+(\\.\\d+)?))(\\-)(\\d+(\\.\\d+)?)"));
        Percent = Pattern.compile(config.getString(getName()+".Pattern").replace("[VALUE]", "((\\-|\\+)?(\\d+(\\.\\d+)?))%"));
        isEnable = config.getBoolean(getName()+".Enable",false);
    }

    @Override
    public boolean isEnable() {
        return isEnable;
    }

    @EventHandler
    public void ListenerAttInhabit(ArathothAttackCDEvent e){
        if(ArathothAPI.getNumAttributeData(e.getEntity(),this.getName()).getPrimary() > 0.0D){
            e.setCD(e.getCD() + (ArathothAPI.getNumAttributeData(e.getEntity(),this.getName()).getPrimary().intValue()));
        }
    }
}
