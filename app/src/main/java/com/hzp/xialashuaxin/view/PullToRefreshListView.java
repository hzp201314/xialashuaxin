package com.hzp.xialashuaxin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hzp.xialashuaxin.R;


public class PullToRefreshListView extends ListView implements OnScrollListener{

	private View headerView;
	private int headermeasuredHeight;
	private RotateAnimation up;
	private RotateAnimation down;
	private int downY;

	/** 下拉刷新的状态 **/
	private final int PULL_DOWN = 1;
	/** 松开刷新的状态 **/
	private final int REFRESH = 2;
	/** 正在刷新的操作 **/
	private final int REFRESHING = 3;
	/** 当前的状态 **/
	private int CURRENTSTATE = PULL_DOWN;
	private TextView mText;
	private ProgressBar mPb;
	private ImageView mArrow;
	
	/**是否加载更多的标示**/
	private boolean isLoadMore;

	public PullToRefreshListView(Context context) {
		// super(context);
		this(context, null);
	}

	// 在布局文件中使用控件的时候调用的方法，AttributeSet保存有控件属性的值
	public PullToRefreshListView(Context context, AttributeSet attrs) {
		// super(context, attrs);
		this(context, attrs, -1);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		addHeader();
		addFooter();
		setOnScrollListener(this);
	}
	
	/**
	 * 给listview底部添加条目
	 *
	 * 2016-10-30 上午10:39:42
	 */
	private void addFooter() {

		footerview = View.inflate(getContext(), R.layout.footer, null);
		
		//隐藏底部条目
		footerview.measure(0, 0);
		footermeasuredHeight = footerview.getMeasuredHeight();
		footerview.setPadding(0, 0, 0, -footermeasuredHeight);
		
		this.addFooterView(footerview);//给listview的底部添加条目
		
	}

	/**
	 * 添加刷新头
	 * 
	 * 2016-10-29 上午11:51:04
	 */
	private void addHeader() {
		headerView = View.inflate(getContext(), R.layout.header, null);

		mArrow = (ImageView) headerView.findViewById(R.id.arrow);
		mPb = (ProgressBar) headerView.findViewById(R.id.pb);
		mText = (TextView) headerView.findViewById(R.id.text);

		// 隐藏刷新头
		// 获取刷新头的高度
		// 手动的测量
		// 测量的规则，如果0：表示不指定宽高，设置多少就是多少
		headerView.measure(0, 0);
		// 获取测量的高度
		headermeasuredHeight = headerView.getMeasuredHeight();
		// 距离父控件的边框的填充距离
		headerView.setPadding(0, -headermeasuredHeight, 0, 0);

		this.addHeaderView(headerView);// 给listview添加头条目

		// 初始化旋转动画
		setAnimation();
	}

	/**
	 * 初始化旋转动画
	 * 
	 * 2016-10-30 上午9:27:20
	 */
	private void setAnimation() {
		// 箭头向上
		up = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		up.setDuration(500);
		up.setFillAfter(true);// 保持动画结束的状态
		// 箭头向下
		down = new RotateAnimation(-180, -360, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		down.setDuration(500);
		down.setFillAfter(true);// 保持动画结束的状态
	}

	// 下拉显示刷新头
	// 1.触摸事件
	// 2.下拉,通过移动的y坐标-按下的y坐标，如果值是大于0表示下拉
	// 3.当前界面显示的第一个条目是listview的第一个条目
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int moveY = (int) ev.getY();
			// 2.下拉,通过移动的y坐标-按下的y坐标，如果值是大于0表示下拉
			int distanceY = moveY - downY;
			// 处理判断是下拉之外，还要检测当前界面看到的第一个条目是否是listview的第一个条目
			// getFirstVisiblePosition : 获取当前界面显示的第一个条目的索引
			if (distanceY > 0 && getFirstVisiblePosition() == 0) {
				// 计算空白的区域
				int paddingTop = distanceY - headermeasuredHeight;
				// 显示刷新头
				headerView.setPadding(0, paddingTop, 0, 0);

				// 如果有空白区域 下拉刷新 -> 松开刷新
				if (paddingTop > 0 && CURRENTSTATE == PULL_DOWN) {
					CURRENTSTATE = REFRESH;
					switchOption();
				}
				// 如果没有空白区域 松开刷新 -> 下拉刷新
				if (paddingTop < 0 && CURRENTSTATE == REFRESH) {
					CURRENTSTATE = PULL_DOWN;
					switchOption();
				}

				// 因为android系统的listview是没有显示空白区域的操作的，如果使用系统的触摸操作实现空白区域操作，会出现计算错误
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			// 如果是松开刷新 -> 正在刷新，并且刷新头显示出来
			if (CURRENTSTATE == REFRESH) {
				CURRENTSTATE = REFRESHING;
				headerView.setPadding(0, 0, 0, 0);
				switchOption();
				//将新的数据刷新出来
				if (listenter != null) {
					listenter.refresh();
				}
			}
			// 如果是下拉刷新 -> 弹出隐藏
			if (CURRENTSTATE == PULL_DOWN) {
				headerView.setPadding(0, -headermeasuredHeight, 0, 0);
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 根据状态改变控件的显示内容
	 * 
	 * 2016-10-30 上午10:18:31
	 */
	private void switchOption() {
		switch (CURRENTSTATE) {
		case PULL_DOWN:
			mArrow.startAnimation(down);
			mText.setText("下拉刷新");
			break;
		case REFRESH:
			mArrow.startAnimation(up);
			mText.setText("松开刷新");
			break;
		case REFRESHING:
			mText.setText("正在刷新");
			mArrow.clearAnimation();//清除动画
			mArrow.setVisibility(View.GONE);
			mPb.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	
	//创建回调，让activity可以监听下拉刷新操作，实现刷新数据操作
	public interface OnRefreshListenter{
		/**
		 * 下拉刷新
		 *
		 * 2016-10-30 上午10:27:30
		 */
		public void refresh();
		/**
		 * 加载更多
		 *
		 * 2016-10-30 上午11:14:12
		 */
		public void loadmore();
	}
	private OnRefreshListenter listenter;
	private View footerview;
	private int footermeasuredHeight;
	public void setOnRefreshListener(OnRefreshListenter listenter){
		this.listenter = listenter;
	}
	
	/**
	 * 取消刷新
	 *
	 * 2016-10-30 上午10:32:40
	 */
	public void finish(){
		//正在刷新 -> 下拉刷新
		if (CURRENTSTATE == REFRESHING) {
			CURRENTSTATE = PULL_DOWN;
			mText.setText("下拉刷新");
			mPb.setVisibility(View.GONE);
			mArrow.setVisibility(View.VISIBLE);
			headerView.setPadding(0, -headermeasuredHeight, 0, 0);
		}
		if (isLoadMore) {
			//取消加载更多的操作
			footerview.setPadding(0, 0, 0, -footermeasuredHeight);
			isLoadMore = false;
		}
		
	}

	//滚动状态改变调用的方法
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//当滚动到listview的最后一个条目的时候，停止滚动，显示加载更多的条目
		//getAdapter() : 获取listview设置的adapter
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && getLastVisiblePosition() == getAdapter().getCount()-1 && isLoadMore == false) {
			isLoadMore = true;
			footerview.setPadding(0, 0, 0, 0);
			//当加载更多条目显示出来的时候，listview的条目数回自动+1
			//需要重新定位最后一个条目
			setSelection(getAdapter().getCount()-1);//定位选择哪个条目position：条目的索引
			//加载更多数据
			if (listenter != null) {
				listenter.loadmore();
			}
		}
	}
	//滚动调用
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
	}
	
	
	
	
	
	
	
	

}
