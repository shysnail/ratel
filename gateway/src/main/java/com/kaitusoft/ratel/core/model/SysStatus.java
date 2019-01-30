package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.util.StringUtils;
import lombok.Data;
import lombok.ToString;
import org.hyperic.sigar.*;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * @author frog.w
 * @version 1.0.0, 2018/8/16
 *          <p>
 *          write description here
 */
@Data
@ToString
public class SysStatus {

    private static final long K_BYTES = 1024;
    private static final long M_BYTES = 1024 * K_BYTES;
    private static final long G_BYTES = 1024 * M_BYTES;
    private static Sigar sigar;
    private String total;
    private String free;

    /** */
    private String max;

    /** */
    private long s1;

    /** */
    private long s2;

    /** */
    private long perm;

    /** */
    /**
     * 操作系统.
     */
    private String osName = System.getProperty("os.name");

    /** */
    /**
     * 总的物理内存.
     */
    private long totalMemorySize;
    /**
     * 剩余的物理内存.
     */
    private long freeMemorySize;
    /**
     * 已使用的物理内存.
     */
    private long usedMemory;
    /**
     * 线程总数.
     */
    private int totalThread;
    /**
     * cpu使用率.
     */
    private double cpuRatio;
    private long inBytes;
    private long outBytes;
    private Map<String, Network> networks = new HashMap<>();

    public static SysStatus collect() {
        sigar = new Sigar();
        SysStatus status = new SysStatus();
        status.totalMemorySize = Runtime.getRuntime().maxMemory();
        status.freeMemorySize = Runtime.getRuntime().freeMemory();
        status.usedMemory = Runtime.getRuntime().totalMemory();

        status.total = SysStatus.toReadableCapacity(status.usedMemory);
        status.free = SysStatus.toReadableCapacity(status.freeMemorySize);
        status.max = SysStatus.toReadableCapacity(status.totalMemorySize);
        return status;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("java.library.path"));

    }

    public static String toReadableCapacity(long bytes) {
        if (bytes > G_BYTES)
            return MessageFormat.format("{0, number, #.##} GB", 1.0 * bytes / G_BYTES);
        else if (bytes > M_BYTES)
            return MessageFormat.format("{0, number, #.##} MB", 1.0 * bytes / M_BYTES);
        else if (bytes > K_BYTES)
            return MessageFormat.format("{0, number, #.##} KB", 1.0 * bytes / K_BYTES);

        return bytes + " B";
    }

    public void collectBasic() {
        sigar = new Sigar();

        totalMemorySize = Runtime.getRuntime().maxMemory();
        freeMemorySize = Runtime.getRuntime().freeMemory();
        usedMemory = Runtime.getRuntime().totalMemory();

        total = SysStatus.toReadableCapacity(totalMemorySize);
        free = SysStatus.toReadableCapacity(freeMemorySize);
        max = SysStatus.toReadableCapacity(usedMemory);

        try {
            cpu();
        } catch (SigarException e) {
            e.printStackTrace();
        }

        try {
            net();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sigar.close();
    }

    private void cpu() throws SigarException {
        CpuInfo infos[] = sigar.getCpuInfoList();
        CpuPerc cpus[] = null;
        cpus = sigar.getCpuPercList();
        double userUse = 0;
        double sysUse = 0;
        double use = 0;
        double wait = 0;
        for (int i = 0; i < infos.length; i++) {// 不管是单块CPU还是多CPU都适用
            CpuInfo info = infos[i];
//            System.out.println("第" + (i + 1) + "块CPU信息");
//            System.out.println("CPU的总量MHz:    " + info.getMhz());// CPU的总量MHz
//            System.out.println("CPU生产商:    " + info.getVendor());// 获得CPU的卖主，如：Intel
//            System.out.println("CPU类别:    " + info.getModel());// 获得CPU的类别，如：Celeron
//            System.out.println("CPU缓存数量:    " + info.getCacheSize());// 缓冲存储器数量
//            printCpuPerc(cpus[i]);
            userUse += cpus[i].getUser();
            sysUse += cpus[i].getSys();
            use += cpus[i].getCombined();
            wait += cpus[i].getWait();
        }

        cpuRatio = userUse / infos.length;
        sysUse = sysUse / infos.length;
//        cpuRatio = use / infos.length;
        wait = wait / infos.length;

    }

    private void printCpuPerc(CpuPerc cpu) {
        System.out.println("CPU用户使用率:    " + CpuPerc.format(cpu.getUser()));// 用户使用率
        System.out.println("CPU系统使用率:    " + CpuPerc.format(cpu.getSys()));// 系统使用率
        System.out.println("CPU当前等待率:    " + CpuPerc.format(cpu.getWait()));// 当前等待率
        System.out.println("CPU当前错误率:    " + CpuPerc.format(cpu.getNice()));//
        System.out.println("CPU当前空闲率:    " + CpuPerc.format(cpu.getIdle()));// 当前空闲率
        System.out.println("CPU总的使用率:    " + CpuPerc.format(cpu.getCombined()));// 总的使用率
    }

    private void net() throws Exception {
        String ifNames[] = sigar.getNetInterfaceList();

        for (int i = 0; i < ifNames.length; i++) {
            String name = ifNames[i];

            NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
            String netmask = ifconfig.getNetmask();
            String broadcast = ifconfig.getBroadcast();
            if (StringUtils.isEmpty(broadcast) || "0.0.0.0".equals(broadcast))
                continue;

//            System.out.println("网络设备名:    " + name);// 网络设备名
//            System.out.println("IP地址:    " + ifconfig.getAddress());// IP地址
//            System.out.println("broadcast:    " + ifconfig.getBroadcast());// 子网掩码
//            System.out.println("子网掩码:    " + ifconfig.getNetmask());// 子网掩码
            if ((ifconfig.getFlags() & 1L) <= 0L) {
                System.out.println("!IFF_UP...skipping getNetInterfaceStat");
                continue;
            }


            Network network = new Network();
            network.setName(name);
            NetInterfaceStat ifstat = sigar.getNetInterfaceStat(name);

            network.setInBytes(ifstat.getRxBytes());
            network.setOutBytes(ifstat.getTxBytes());
            network.setInPackets(ifstat.getRxPackets());
            network.setOutPackets(ifstat.getTxPackets());
            networks.put(name, network);
//            System.out.println(name + "接收的总包裹数:" + ifstat.getRxPackets());// 接收的总包裹数
//            System.out.println(name + "发送的总包裹数:" + ifstat.getTxPackets());// 发送的总包裹数
//            System.out.println(name + "接收到的总字节数:" + ifstat.getRxBytes());// 接收到的总字节数
//            System.out.println(name + "发送的总字节数:" + ifstat.getTxBytes());// 发送的总字节数
//            System.out.println(name + "接收到的错误包数:" + ifstat.getRxErrors());// 接收到的错误包数
//            System.out.println(name + "发送数据包时的错误数:" + ifstat.getTxErrors());// 发送数据包时的错误数
//            System.out.println(name + "接收时丢弃的包数:" + ifstat.getRxDropped());// 接收时丢弃的包数
//            System.out.println(name + "发送时丢弃的包数:" + ifstat.getTxDropped());// 发送时丢弃的包数
        }

    }


}
