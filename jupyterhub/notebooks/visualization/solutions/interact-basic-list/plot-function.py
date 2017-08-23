@interact(k=(0.5, 2), p=(0, 2 * np.pi))
def f(k, p):
    x = np.linspace(0, 4 * np.pi)
    y = np.sin(k*x + p)
    plt.plot(x, y)
