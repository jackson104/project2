import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Bombgame extends JFrame implements ActionListener{
    
    static int mapHeight = 9, mapWidth = 9, bombNum = 10;
    static int frameWidth = 400, frameHeight = 400;
    private boolean[][] isTurned;
    private boolean gameOver;
    private boolean gameRunning;
    private int[][] aroundBombNum;
    private boolean[][] map;
    private boolean[][] isPressed;
    private JButton[][] buttons;
    private JPanel centerButtonPanel;
    private JLabel gameMessage;

    
    public Bombgame()
    {
        super("踩地雷");
        setSize(frameWidth, frameHeight);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setLayout(new BorderLayout());
        
        gameMessage = new JLabel("遊戲進行中");//訊息顯示 
        
        add(gameMessage, BorderLayout.NORTH);
        
        centerButtonPanel = new JPanel();
        centerButtonPanel.setLayout(new GridLayout(mapHeight, mapWidth));
        buttons = new JButton[mapHeight][mapWidth];
        
        //按鈕
        for(int i=0; i<mapHeight; i++)
            for(int j=0; j<mapWidth; j++)
            {
                buttons[i][j] = new JButton();
                buttons[i][j].addActionListener(this);
                buttons[i][j].setBackground(Color.WHITE);
                buttons[i][j].setActionCommand(i+" "+j);
                centerButtonPanel.add(buttons[i][j]);
            }
        
        add(centerButtonPanel, BorderLayout.CENTER);
        
        JButton re = new JButton("陳祐豪我幹你娘重新吧廢物");
        re.setActionCommand("regame");
        re.addActionListener(this);
        
        add(re, BorderLayout.SOUTH);
        
        gameRunning = false;
    }
    
    public void resetGame(int sx,int sy)//清空地圖、隨機地圖、重設按鈕文字
    {
        gameOver = false;
        aroundBombNum = new int[mapHeight][mapWidth];//清空地圖
        map = new boolean[mapHeight][mapWidth];
        isPressed = new boolean[mapHeight][mapWidth];
        isTurned = new boolean[mapHeight][mapWidth];
        
        int bombCount = 0;
        while(bombCount < bombNum)
        {
            int x = (int)(Math.random()*mapHeight);
            int y = (int)(Math.random()*mapWidth);
            if(sx!=x|sy!=y)
            if(!map[x][y])//沒炸彈
            {
                bombCount++;//放了炸彈
                map[x][y] = true;
                //向周圍宣布有炸彈 
                for(int i=x-1; i<=x+1; i++)
                    for(int j = y-1; j<=y+1; j++)
                    {
                        if(i>=0&&i<mapHeight&&j>=0&&j<mapWidth)
                            aroundBombNum[i][j]++;
                    }
            }
        }
            
        System.out.println("地圖建置成功");
        printMap();
    }
    
    private void clean()
    {
        for(int i=0; i<mapHeight; i++)
            for(int j=0; j<mapWidth; j++)
            {
                buttons[i][j].setText("");
                buttons[i][j].setBackground(Color.WHITE);
            }
    }
    
    private void printMap()
    {
        System.out.println("Bombs");
        for(int i=0; i<mapHeight; i++)
        {
            for(int j=0; j<mapWidth; j++)
            {
                if(map[i][j])
                    System.out.print("*");
                else
                    System.out.print("_");
            }
            System.out.println();
        }
        System.out.println("Arounds");
        for(int i=0; i<mapHeight; i++)
        {
            for(int j=0; j<mapWidth; j++)
            {
                System.out.print(aroundBombNum[i][j]);
            }
            System.out.println();
        }
    }

    public void freshMessage(String newMessage)
    {
        System.out.println(newMessage);
        gameMessage.setText(newMessage);
    }
    
    public void click(int x, int y)//炸彈: 爆炸| 土地 0擴散 1~8不擴散
    {
        if(map[x][y])//炸彈
        {
            freshMessage("踩到地雷! 爆炸!");
            buttons[x][y].setBackground(Color.RED);
            gameOver = true;
            return ;
        }
        else
        if(aroundBombNum[x][y]!=0)
        {
            turn(x,y);
        }
        else
        {
            int[][] direct = {{-1,0},{0,-1},{1,0},{0,1},{-1,-1},{1,1},{-1,1},{1,-1}};//上 左 下 右 左上 左下 右上 右下
            int[] queue_x = new int[mapHeight*mapWidth];
            int[] queue_y = new int[mapHeight*mapWidth];
            int pop = 0, push = 0;
            
            queue_x[push] = x;//填入第一個
            queue_y[push] = y;
            push++;
            
            while(pop < push)
            {
                int tx = queue_x[pop];
                int ty = queue_y[pop];
                
                //如果 此格是0就繼續擴散
                
                if(aroundBombNum[tx][ty]==0)
                for(int s=0; s<8; s++)
                {
                    int fx = tx+direct[s][0];
                    int fy = ty+direct[s][1];
                    boolean spread = false;
                    
                    if(inRange(fx,fy)&&!isTurned[fx][fy])
                    {
                            isTurned[fx][fy] = true;
                            queue_x[push] = fx;
                            queue_y[push] = fy;
                            push++;
                    }
                }
                    
                pop++;
                
                /*System.out.println("Pop"+pop);
                for(int i=0; i<push; i++)
                {
                    System.out.print(" "+"["+queue_x[i]+"]"+ "["+queue_y[i]+"]");
                }
                System.out.println();*/
            }
            
            for(int i=0; i<push; i++)
                turn(queue_x[i],queue_y[i]);
            
        }
    }
    
    private static boolean inRange(int x, int y)
    {
        return (x>=0&&x<mapHeight&&y>=0&&y<mapWidth);
    }
    
    public void turn(int x, int y)//翻開
    {
        buttons[x][y].setBackground(Color.LIGHT_GRAY);
        if(aroundBombNum[x][y]>0)
            buttons[x][y].setText(Integer.toString(aroundBombNum[x][y]));
        isTurned[x][y] = true;
        isPressed[x][y] = true;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        String command =e.getActionCommand();
        
        
        if(command.equals("regame"))
        {
            clean();
            gameRunning = false;
            freshMessage("遊戲進行中");
        }
        else
        {
        
            String[] press = command.split(" ");
            int x = Integer.parseInt(press[0]);
            int y = Integer.parseInt(press[1]);
            
            if(!gameRunning)
            {
                resetGame(x,y);
                gameRunning = true;
            }
            if(!isPressed[x][y]&&!gameOver)
            {
                System.out.println("Invoke click "+x+" "+y);
                click(x,y);
                isPressed[x][y] = true;
            }
        }
    }
    
    public static void main(String[] args)
    {
        Bombgame frame = new Bombgame();
        frame.setVisible(true);
    }
    
}