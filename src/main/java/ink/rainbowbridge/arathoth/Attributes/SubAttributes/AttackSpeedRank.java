package ink.rainbowbridge.arathoth.Attributes.SubAttributes;

import ink.rainbowbridge.arathoth.API.ArathothAPI;
import ink.rainbowbridge.arathoth.Arathoth;
import ink.rainbowbridge.arathoth.Attributes.SpecialAttribute;
import ink.rainbowbridge.arathoth.Events.ArathothAttackCDEvent;
import ink.rainbowbridge.arathoth.Utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SpecialAttribute 注册实例
 * 0.1.2时代修复了切刀忽视冷却的漏洞
 * 现在可以通过监听AttackC事件来修改一切参数
 *
 * 0.1.3 修复卡线程
 *
 * @author 寒雨
 * @create 2020/12/12 13:03
 */
public class AttackSpeedRank implements SpecialAttribute, Listener {
    private boolean isEnable;
    private Pattern pattern;
    private FileConfiguration config;
    private HashMap<String,Integer> rankList;
    private HashMap<UUID, Material> CDmap;
    @Override
    public Object parseValue(LivingEntity e) {
        if(e instanceof Player){
            Player p = (Player)e;
            String Rank = null;
            for(String str : ItemUtils.getUncoloredLore(p.getInventory().getItemInMainHand())){
                Matcher m = pattern.matcher(str);
                if (m.find()){
                    Rank = m.group(1);
                    break;
                }
            }
        if (rankList.containsKey(Rank)){
            return Rank;
        }
        else{
            return config.getString(getName()+".Settings.DefaultRank.Name","Normal");
        }
        }
        return null;
    }

    @Override
    public void function(Event e, LivingEntity entity) {
        if(isEnable()){
            if (e instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
                if (event.getDamager() instanceof Player) {
                    Player p = (Player) event.getDamager();
                    String rank = (String) parseValue(p);
                    ArathothAttackCDEvent eve = new ArathothAttackCDEvent(p, rankList.get(rank),(LivingEntity) event.getEntity());
                    Bukkit.getPluginManager().callEvent(eve);
                    if (eve.getCD() > 0) {
                        eve.getPlayer().setCooldown(eve.getPlayer().getInventory().getItemInMainHand().getType(), eve.getCD());
                        CDmap.put(eve.getPlayer().getUniqueId(),eve.getPlayer().getInventory().getItemInMainHand().getType());
                        Arathoth.Debug("触发攻击冷却 (AttackCD) &f[" + eve.getPlayer().getName() + "] [" + eve.getCD() + "]");
                    }
                }
            }
        }
    }

    @Override
    public String getPlaceHolder(Player p) {
        return (String)parseValue(p);
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
            config.set(getName()+".Settings.DefaultRank.Name", "Normal");
            config.set(getName()+".Settings.DefaultRank.value", 20);
            config.set(getName()+".Settings.RegisterRank.Slow", 30);
            config.set(getName()+".Settings.RegisterRank.VerySlow", 40);
            config.set(getName()+".Settings.RegisterRank.Fast", 10);
            config.set(getName()+".Settings.RegisterRank.VeryFast", 5);
            ArathothAPI.saveAttributeConfig(this,config);
        }
        else{
            config = ArathothAPI.getAttributeConfig(this);
        }
        Bukkit.getPluginManager().registerEvents(this, Arathoth.getInstance());
        rankList = new HashMap<>();
        rankList.put(config.getString(getName()+".Settings.DefaultRank.Name","Normal"),config.getInt(getName()+".Settings.DefaultRank.value",20));

        pattern = Pattern.compile(config.getString(getName()+".Pattern").replace("[VALUE]", "(\\S+)"));
        isEnable = config.getBoolean(getName()+".Enable",false);
        for(String str : config.getConfigurationSection(getName()+".Settings.RegisterRank").getKeys(false)){
            rankList.put(str,config.getInt(getName()+".Settings.RegisterRank."+str));
        }
        new BukkitRunnable() {
            @Override
            public void run() {
            CDmap = new HashMap<>();
            Arathoth.Debug("清理攻速数据.....");
            }
        }.runTaskTimer(Arathoth.getInstance(),12000,12000);
    }

    @Override
    public boolean isEnable() {
        return isEnable;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackCD(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player){
            Player p = (Player)e.getDamager();
            if (CDmap.containsKey(p.getUniqueId())) {
                if (p.getCooldown(CDmap.get(p.getUniqueId())) == 0) {
                    function(e, null);
                }
                else{
                    e.setCancelled(true);
                }
            }
            else{
                function(e,null);
            }
        }
    }
}
